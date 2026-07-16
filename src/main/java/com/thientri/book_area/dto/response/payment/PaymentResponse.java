package com.thientri.book_area.dto.response.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
	private Long id;
	private String orderCode; // Trích xuất từ Order
	private String paymentMethodName; // Trích xuất từ PaymentMethod
	private BigDecimal amount;
	private String status;
	private String transactionId; // Để người dùng dễ tra cứu với ngân hàng
	private LocalDateTime paidAt;
}
