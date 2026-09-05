package com.thientri.book_area.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
	@NotBlank(message = "Email không được để trống")
	@Email(message = "Email không đúng định dạng")
	private String email;

	@NotBlank(message = "Mật khẩu không được để trống")
	@Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
	private String password;

	@NotBlank(message = "Họ và tên không được để trống")
	private String fullName;

	@Pattern(regexp = "^(0[35789]\\d{8}|02\\d{9})?$", message = "Số điện thoại không hợp lệ (Di động 10 số hoặc cố định 11 số bắt đầu từ số 0)")
	private String phone;

	private String otp;
}
