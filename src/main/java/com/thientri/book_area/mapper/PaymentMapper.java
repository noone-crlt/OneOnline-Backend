package com.thientri.book_area.mapper;

import org.springframework.stereotype.Component;
import com.thientri.book_area.dto.response.payment.PaymentResponse;
import com.thientri.book_area.model.payment.Payment;

@Component
public class PaymentMapper {

    public PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) return null;

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderCode(payment.getOrder() != null ? payment.getOrder().getOrderCode() : "UNKNOWN")
                .paymentMethodName(payment.getPaymentMethod() != null ? payment.getPaymentMethod().getName() : "UNKNOWN")
                .amount(payment.getAmount())
                .status(payment.getStatus() != null ? payment.getStatus().name() : "UNKNOWN")
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}