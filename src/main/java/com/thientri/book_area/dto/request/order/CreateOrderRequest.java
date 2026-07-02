package com.thientri.book_area.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long addressId;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private Long paymentMethodId;

    // Mã giảm giá là tùy chọn (có thể null)
    private String couponCode;
}
