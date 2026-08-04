package com.thientri.book_area.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressRequest {
	@NotBlank(message = "Tên người nhận không được trống")
	private String recipientName;

	@NotBlank(message = "Số điện thoại không được trống")
	@Pattern(regexp = "^(0[35789]\\d{8}|02\\d{9})$", message = "Số điện thoại không hợp lệ (Di động 10 số hoặc cố định 11 số bắt đầu từ số 0)")
	private String recipientPhone;

	@NotBlank(message = "Địa chỉ chi tiết không được trống")
	private String addressLine;

	@NotBlank
	private String provinceId;
	@NotBlank
	private String provinceName;
	@NotBlank
	private String districtId;
	@NotBlank
	private String districtName;
	@NotBlank
	private String wardId;
	@NotBlank
	private String wardName;

	private Boolean isDefault;
}
