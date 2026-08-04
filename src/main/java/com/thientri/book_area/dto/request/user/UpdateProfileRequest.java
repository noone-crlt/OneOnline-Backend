package com.thientri.book_area.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
	@NotBlank(message = "Họ và tên không được để trống")
	@Size(max = 255, message = "Họ và tên không được vượt quá 255 ký tự")
	private String fullName;

	@Pattern(regexp = "^(0[35789]\\d{8}|02\\d{9})?$", message = "Số điện thoại không hợp lệ (Di động 10 số hoặc cố định 11 số bắt đầu từ số 0)")
	private String phone;
}
