package com.thientri.book_area.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
	@NotBlank(message = "Họ và tên không được để trống")
	@Size(max = 255, message = "Họ và tên không được vượt quá 255 ký tự")
	private String fullName;

	@Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
	private String phone;
}
