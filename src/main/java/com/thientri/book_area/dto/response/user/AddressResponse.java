package com.thientri.book_area.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
	private Long id;
	private String recipientName;
	private String recipientPhone;
	private String addressLine;
	private String provinceId;
	private String provinceName;
	private String districtId;
	private String districtName;
	private String wardId;
	private String wardName;
	private Boolean isDefault;
}
