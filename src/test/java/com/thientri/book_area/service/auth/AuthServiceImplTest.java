package com.thientri.book_area.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thientri.book_area.dto.request.user.UpdateProfileRequest;
import com.thientri.book_area.mapper.UserMapper;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.user.RefreshTokenRepository;
import com.thientri.book_area.repository.user.RoleRepository;
import com.thientri.book_area.repository.user.UserRepository;
import com.thientri.book_area.security.JwtService;
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
}
