package com.thientri.book_area.service.auth;

import com.thientri.book_area.dto.request.user.LoginRequest;
import com.thientri.book_area.dto.request.user.GoogleLoginRequest;
import com.thientri.book_area.dto.request.user.RegisterRequest;
import com.thientri.book_area.dto.request.user.UpdateProfileRequest;
import com.thientri.book_area.dto.response.user.AuthResponse;
import com.thientri.book_area.model.user.User;

public interface IAuthService {
	void register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	AuthResponse loginWithGoogle(GoogleLoginRequest request);

	User updateProfile(User currentUser, UpdateProfileRequest request);
}
