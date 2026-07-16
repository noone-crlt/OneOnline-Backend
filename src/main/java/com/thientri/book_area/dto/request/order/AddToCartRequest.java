package com.thientri.book_area.dto.request.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
	@NotNull(message = "ID phiên bản sách không được để trống")
	private Long editionId;

	@NotNull(message = "Số lượng không được để trống")
	@Min(value = 1, message = "Số lượng tối thiểu phải là 1")
	private Integer quantity;
}
