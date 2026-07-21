package com.thientri.book_area.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thientri.book_area.dto.request.user.GoogleLoginRequest;
import com.thientri.book_area.dto.request.user.UpdateProfileRequest;
import com.thientri.book_area.dto.response.user.AuthResponse;
import com.thientri.book_area.mapper.UserMapper;
import com.thientri.book_area.model.user.Role;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.model.user.UserStatus;
import com.thientri.book_area.repository.user.RefreshTokenRepository;
import com.thientri.book_area.repository.user.RoleRepository;
import com.thientri.book_area.repository.user.UserRepository;
import com.thientri.book_area.security.JwtService;
import com.thientri.book_area.service.auth.GoogleIdentityService.GoogleUserInfo;
import com.thientri.book_area.service.auth.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private RoleRepository roleRepository;
	@Mock
	private RefreshTokenRepository refreshTokenRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private UserMapper userMapper;
	@Mock
	private GoogleIdentityService googleIdentityService;
	@InjectMocks
	private AuthServiceImpl authService;

	@Test
	void updateProfileTrimsAndPersistsProfileFields() {
		User principal = User.builder().id(7L).build();
		User storedUser = User.builder().id(7L).fullName("Tên cũ").phone("0900000000").build();
		UpdateProfileRequest request = new UpdateProfileRequest();
		request.setFullName("  Nguyễn Văn An  ");
		request.setPhone(" 0912345678 ");
		when(userRepository.findById(7L)).thenReturn(Optional.of(storedUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User result = authService.updateProfile(principal, request);

		assertEquals("Nguyễn Văn An", result.getFullName());
		assertEquals("0912345678", result.getPhone());
	}

	@Test
	void updateProfileStoresBlankPhoneAsNull() {
		User principal = User.builder().id(8L).build();
		User storedUser = User.builder().id(8L).phone("0900000000").build();
		UpdateProfileRequest request = new UpdateProfileRequest();
		request.setFullName("Nguyễn Văn Bình");
		request.setPhone("   ");
		when(userRepository.findById(8L)).thenReturn(Optional.of(storedUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User result = authService.updateProfile(principal, request);

		assertNull(result.getPhone());
	}

	@Test
	void loginWithGoogleUsesExistingVerifiedEmailAndReturnsInternalSession() {
		GoogleLoginRequest request = new GoogleLoginRequest();
		request.setCredential("google-id-token");
		Role role = Role.builder().id(1L).name("USER").build();
		User user = User.builder().id(9L).email("reader@example.com").password("encoded")
				.fullName("Bạn đọc").status(UserStatus.ACTIVE).roles(new HashSet<>(Set.of(role))).build();
		AuthResponse expected = AuthResponse.builder().accessToken("access-token").refreshToken("refresh-token")
				.userId(9L).email(user.getEmail()).fullName(user.getFullName()).roles(java.util.List.of("USER")).build();

		when(googleIdentityService.verify("google-id-token"))
				.thenReturn(new GoogleUserInfo("reader@example.com", "Bạn đọc"));
		when(userRepository.findByEmailIgnoreCase("reader@example.com")).thenReturn(Optional.of(user));
		when(jwtService.generateToken(user)).thenReturn("access-token");
		when(userMapper.toAuthResponse(eq(user), eq("access-token"), anyString()))
				.thenReturn(expected);

		AuthResponse result = authService.loginWithGoogle(request);

		assertSame(expected, result);
	}
}
