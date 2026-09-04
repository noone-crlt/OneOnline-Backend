package com.thientri.book_area.service.auth.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.request.user.GoogleLoginRequest;
import com.thientri.book_area.dto.request.user.LoginRequest;
import com.thientri.book_area.dto.request.user.RegisterRequest;
import com.thientri.book_area.dto.request.user.UpdateProfileRequest;
import com.thientri.book_area.dto.response.user.AuthResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.mapper.UserMapper;
import com.thientri.book_area.model.user.RefreshToken;
import com.thientri.book_area.model.user.Role;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.model.user.UserStatus;
import com.thientri.book_area.repository.user.RefreshTokenRepository;
import com.thientri.book_area.repository.user.RoleRepository;
import com.thientri.book_area.repository.user.UserRepository;
import com.thientri.book_area.security.JwtService;
import com.thientri.book_area.service.auth.GoogleIdentityService;
import com.thientri.book_area.service.auth.GoogleIdentityService.GoogleUserInfo;
import com.thientri.book_area.service.auth.IAuthService;

import com.thientri.book_area.dto.request.user.ForgotPasswordRequest;
import com.thientri.book_area.dto.request.user.VerifyOtpRequest;
import com.thientri.book_area.dto.request.user.ResetPasswordRequest;
import com.thientri.book_area.service.notification.IEmailService;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

	// Tiêm (Inject) các dependencies một cách an toàn thông qua Lombok
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final RefreshTokenRepository refreshTokenRepository; // Thêm repo này
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService; // Thêm JWT Service
	private final AuthenticationManager authenticationManager; // Thêm Auth Manager
	private final UserMapper userMapper; // Thêm Mapper
	private final GoogleIdentityService googleIdentityService;
	private final IEmailService emailService;

	private final ConcurrentHashMap<String, OtpData> otpStorage = new ConcurrentHashMap<>();
	private final SecureRandom secureRandom = new SecureRandom();

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class OtpData {
		private String code;
		private LocalDateTime expiryTime;
		private boolean verified;
	}

	@Override
	@Transactional // Đảm bảo tính toàn vẹn: Lỗi ở bất kỳ dòng nào thì rollback toàn bộ
	public void register(RegisterRequest request) {

		// 1. Kiểm tra nghiệp vụ: Email đã tồn tại chưa?
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BadRequestException("Email này đã được sử dụng trong hệ thống.");
		}

		// 2. Tìm quyền mặc định (ROLE_USER) cho tài khoản mới
		Role userRole = roleRepository.findByName("USER")
				.orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy quyền ROLE_USER"));

		Set<Role> roles = new HashSet<>();
		roles.add(userRole);

		// 3. Chuyển đổi DTO thành Entity bằng Builder
		User newUser = User.builder().email(request.getEmail())
				// BẮT BUỘC: Mã hóa mật khẩu trước khi lưu xuống DB
				.password(passwordEncoder.encode(request.getPassword())).fullName(request.getFullName())
				.phone(request.getPhone()).status(UserStatus.ACTIVE).roles(roles).build();

		// 5. Lưu xuống Database
		userRepository.save(newUser);
	}

	@Override
	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng."));

		if (user.getStatus() == UserStatus.BANNED) {
			String formattedDate = user.getBannedAt() != null
					? user.getBannedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
					: "chưa xác định";
			String reason = (user.getBanReason() != null && !user.getBanReason().isBlank())
					? user.getBanReason()
					: "Vi phạm quy định của hệ thống.";
			throw new BadRequestException("Tài khoản của bạn đã bị khóa vào lúc " + formattedDate + ". Lý do khóa: " + reason);
		}

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		return createSession(user);
	}

	@Override
	@Transactional
	public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
		GoogleUserInfo googleUser = googleIdentityService.verify(request.getCredential());
		User user = userRepository.findByEmailIgnoreCase(googleUser.email())
				.orElseGet(() -> createGoogleUser(googleUser));

		if (user.getStatus() == UserStatus.BANNED) {
			String formattedDate = user.getBannedAt() != null
					? user.getBannedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
					: "chưa xác định";
			String reason = (user.getBanReason() != null && !user.getBanReason().isBlank())
					? user.getBanReason()
					: "Vi phạm quy định của hệ thống.";
			throw new BadRequestException("Tài khoản của bạn đã bị khóa vào lúc " + formattedDate + ". Lý do khóa: " + reason);
		}

		if ((user.getFullName() == null || user.getFullName().isBlank()) && !googleUser.fullName().isBlank()) {
			user.setFullName(googleUser.fullName());
			user = userRepository.save(user);
		}

		return createSession(user);
	}

	// Helper method xử lý tạo Refresh Token an toàn
	@Override
	@Transactional
	public User updateProfile(User currentUser, UpdateProfileRequest request) {
		User user = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản."));

		user.setFullName(request.getFullName().trim());
		String phone = request.getPhone();
		user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
		return userRepository.save(user);
	}

	private String generateAndSaveRefreshToken(User user) {
		// Thu hồi (xóa) các token cũ của thiết bị trước để tránh spam rác DB
		refreshTokenRepository.deleteByUser(user);

		// Sinh một chuỗi ngẫu nhiên, độc nhất làm Refresh Token
		String tokenString = UUID.randomUUID().toString();

		RefreshToken refreshToken = RefreshToken.builder().user(user).refreshToken(tokenString)
				.expiryDate(LocalDateTime.now().plusDays(7)) // Tuổi thọ 7 ngày
				.revoked(false).build();

		refreshTokenRepository.save(refreshToken);

		return tokenString;
	}

	private User createGoogleUser(GoogleUserInfo googleUser) {
		Role userRole = roleRepository.findByName("USER")
				.orElseThrow(() -> new IllegalStateException("Không tìm thấy quyền USER mặc định."));

		String fullName = googleUser.fullName().isBlank()
				? googleUser.email().substring(0, googleUser.email().indexOf('@'))
				: googleUser.fullName();

		return userRepository.save(User.builder().email(googleUser.email().toLowerCase(Locale.ROOT))
				.password(passwordEncoder.encode(UUID.randomUUID().toString())).fullName(fullName)
				.status(UserStatus.ACTIVE).roles(new HashSet<>(Set.of(userRole))).build());
	}

	private AuthResponse createSession(User user) {
		if (!user.isEnabled() || !user.isAccountNonLocked()) {
			throw new BadRequestException("Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa.");
		}

		String accessToken = jwtService.generateToken(user);
		String refreshToken = generateAndSaveRefreshToken(user);
		return userMapper.toAuthResponse(user, accessToken, refreshToken);
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new BadRequestException("Địa chỉ email này chưa được đăng ký trong hệ thống."));

		if (user.getStatus() == UserStatus.BANNED) {
			throw new BadRequestException("Tài khoản của bạn đã bị khóa.");
		}

		// Sinh mã OTP 6 chữ số ngẫu nhiên
		String otpCode = String.format("%06d", secureRandom.nextInt(1000000));

		// Lưu OTP có hiệu lực 10 phút
		otpStorage.put(email, new OtpData(otpCode, LocalDateTime.now().plusMinutes(10), false));

		// Gửi email OTP
		emailService.sendOtpEmail(user.getEmail(), otpCode);
	}

	@Override
	public void verifyOtp(VerifyOtpRequest request) {
		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
		OtpData otpData = otpStorage.get(email);

		if (otpData == null || LocalDateTime.now().isAfter(otpData.getExpiryTime())) {
			throw new BadRequestException("Mã OTP đã hết hạn hoặc không tồn tại. Vui lòng gửi lại yêu cầu.");
		}

		if (!otpData.getCode().equals(request.getOtp().trim())) {
			throw new BadRequestException("Mã OTP không chính xác. Vui lòng kiểm tra lại.");
		}

		otpData.setVerified(true);
	}

	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
		OtpData otpData = otpStorage.get(email);

		if (otpData == null || LocalDateTime.now().isAfter(otpData.getExpiryTime()) || !otpData.isVerified()) {
			throw new BadRequestException("Mã OTP chưa được xác thực hoặc đã hết hạn. Vui lòng thực hiện lại.");
		}

		if (!otpData.getCode().equals(request.getOtp().trim())) {
			throw new BadRequestException("Mã OTP không hợp lệ.");
		}

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản để đặt lại mật khẩu."));

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);

		// Hủy bỏ OTP sau khi đã sử dụng thành công
		otpStorage.remove(email);
	}
}
