package com.thientri.book_area.model.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.thientri.book_area.model.order.Order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", precision = 18, scale = 0, nullable = false)
    private BigDecimal amount;

    // SỬA: Ép kiểu Enum an toàn tuyệt đối
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // THÊM MỚI: Cực kỳ quan trọng để đối soát với VNPay/MoMo
    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    // THÊM MỚI: Lưu log JSON phản hồi từ cổng thanh toán (để debug khi có lỗi)
    @Column(name = "gateway_response", columnDefinition = "NVARCHAR(MAX)")
    private String gatewayResponse;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}