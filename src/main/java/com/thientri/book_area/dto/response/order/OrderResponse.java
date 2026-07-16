package com.thientri.book_area.dto.response.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
	private String orderCode;
	private String status; // Lấy từ OrderStatus.name

	// Giao hàng
	private String recipientName;
	private String recipientPhone;
	private String fullShippingAddress; // Gộp chung thành 1 chuỗi cho gọn
	private String trackingCode;

	// Kế toán
	private BigDecimal subTotal;
	private BigDecimal shippingFee;
	private String appliedCouponCode;
	private BigDecimal discountAmount;
	private BigDecimal totalAmount;

	private LocalDateTime createdAt;

	// Danh sách sản phẩm đã mua
	private List<CartItemResponse> orderItems; // Tái sử dụng form hiển thị giống CartItem
}
