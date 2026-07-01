package com.thientri.book_area.service.auth;

import com.thientri.book_area.dto.request.user.LoginRequest;
import com.thientri.book_area.dto.request.user.RegisterRequest;
import com.thientri.book_area.dto.response.user.AuthResponse;

public interface IAuthService {
    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}