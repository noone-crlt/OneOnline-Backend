package com.thientri.book_area.dto.request.payment;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookRequest {
	private Long id;
	private String gateway;
	private String transactionDate;
	private String accountNumber;
	private String code;
	private String content;
	private String transferType;
	private BigDecimal transferAmount;
	private String referenceCode;
}
