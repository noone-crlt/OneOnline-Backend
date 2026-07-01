package com.thientri.book_area.model.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã phải bắt buộc có và không trùng lặp
    @Column(name = "code", length = 50, unique = true, nullable = false)
    private String code;
    
    // Ép kiểu Enum để an toàn tuyệt đối
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", precision = 18, scale = 0, nullable = false)
    private BigDecimal discountValue;

    @Column(name = "max_discount", precision = 18, scale = 0)
    private BigDecimal maxDiscount;

    // THÊM MỚI: Đồng bộ với Database bản 3
    @Column(name = "min_order_value", precision = 18, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
}