package com.thientri.book_area.controller.auth;

import com.thientri.book_area.dto.response.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.dto.request.user.LoginRequest;
import com.thientri.book_area.dto.request.user.GoogleLoginRequest;
import com.thientri.book_area.dto.request.user.RegisterRequest;
import com.thientri.book_area.dto.request.user.UpdateProfileRequest;
import com.thientri.book_area.dto.response.user.AuthResponse;
import com.thientri.book_area.model.user.Role;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.service.auth.IAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final IAuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
		authService.register(request);
		return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công!", null));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/google")
	public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
			@Valid @RequestBody GoogleLoginRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.loginWithGoogle(request)));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(ApiResponse.success(profileResponse(user)));
	}

	@PatchMapping("/me")
	public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(@AuthenticationPrincipal User user,
			@Valid @RequestBody UpdateProfileRequest request) {
		return ResponseEntity.ok(ApiResponse.success(profileResponse(authService.updateProfile(user, request))));
	}

	private Map<String, Object> profileResponse(User user) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("id", user.getId());
		response.put("email", user.getEmail());
		response.put("fullName", user.getFullName());
		response.put("phone", user.getPhone());
		response.put("roles", user.getRoles().stream().map(Role::getName).toList());
		return response;
	}
}
