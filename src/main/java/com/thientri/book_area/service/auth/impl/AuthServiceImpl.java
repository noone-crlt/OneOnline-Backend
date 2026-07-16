package com.thientri.book_area.service.auth.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.thientri.book_area.service.auth.IAuthService;

import lombok.RequiredArgsConstructor;

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

		// 1. Kích hoạt Spring Security kiểm tra Email và Password
		// Nếu sai, nó sẽ tự động ném ra BadCredentialsException (Sai mật khẩu/tài
		// khoản)
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		// 2. Lấy thông tin User từ Database (Chắc chắn tồn tại vì bước 1 đã qua)
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new BadRequestException("Tài khoản không tồn tại."));

		// Kiểm tra xem user có bị xóa mềm hay ban không
		if (!user.isEnabled() || !user.isAccountNonLocked()) {
			throw new BadRequestException("Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa.");
		}

		// 3. Tạo Access Token (Tuổi thọ ngắn)
		String accessToken = jwtService.generateToken(user);

		// 4. Sinh Refresh Token (Tuổi thọ dài) và lưu vào Database
		String refreshToken = generateAndSaveRefreshToken(user);

		// 5. Đóng gói dữ liệu trả về Frontend
		return userMapper.toAuthResponse(user, accessToken, refreshToken);
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
}
