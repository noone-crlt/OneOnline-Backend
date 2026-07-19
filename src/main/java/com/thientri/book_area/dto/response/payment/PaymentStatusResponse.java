package com.thientri.book_area.dto.response.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatusResponse {
	private String orderCode;
	private String status;
	private String paymentMethod;
	private String paymentUrl;
	private String bankCode;
	private String accountNumber;
	private String accountName;
	private BigDecimal amount;
	private String transferContent;
	private OffsetDateTime expiresAt;
}
