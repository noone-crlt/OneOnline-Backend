package com.thientri.book_area.mapper;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.thientri.book_area.dto.response.user.AuthResponse;
import com.thientri.book_area.model.user.Role;
import com.thientri.book_area.model.user.User;

@Component
public class UserMapper {

    public AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        if (user == null) return null;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();
    }
}