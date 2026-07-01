package com.thientri.book_area.dto.response.user;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    
    // Thông tin cơ bản để Frontend hiển thị trên Header (Góc phải màn hình)
    private Long userId;
    private String email;
    private String fullName;
    private List<String> roles; // Chỉ trả về List String, ví dụ: ["ROLE_USER", "ROLE_ADMIN"]
}