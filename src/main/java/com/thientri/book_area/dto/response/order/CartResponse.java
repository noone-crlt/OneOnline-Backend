package com.thientri.book_area.dto.response.order;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponse {
	private Long cartId;
	private List<CartItemResponse> items;
	private Integer totalItems;
	private BigDecimal cartTotalAmount; // Tổng tiền giỏ hàng (chưa tính phí ship/khuyến mãi)
}
