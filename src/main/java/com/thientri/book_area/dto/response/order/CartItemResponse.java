package com.thientri.book_area.dto.response.order;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long id; // ID của dòng giỏ hàng
    private Long editionId;
    private String bookTitle;       // Flattening từ Book
    private String format;          // PHYSICAL, AUDIOBOOK...
    private String coverImageUrl;   // Ảnh bìa để hiển thị
    private BigDecimal salePrice;   // Giá lúc hiển thị
    private Integer quantity;
    private BigDecimal subTotal;    // Backend tự nhân: salePrice * quantity
}