package com.thientri.book_area.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
	@NotBlank(message = "Thông tin xác thực Google không được để trống.")
	private String credential;
}
