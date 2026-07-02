package com.thientri.book_area.dto.response.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private String orderCode;
    private String paymentStatus;
    private String paymentUrl;
}
