package com.thientri.book_area.dto.request.catalog;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EditionCreateRequest {

    @NotNull(message = "ID sách gốc không được để trống")
    private Long bookId;

    @NotBlank(message = "Định dạng sách không được để trống")
    @Pattern(regexp = "^(PHYSICAL|EBOOK_PDF|EBOOK_EPUB|AUDIOBOOK)$", message = "Định dạng không hợp lệ")
    private String format;

    @NotBlank(message = "Mã SKU không được để trống")
    private String skuCode;

    // Tiền tệ VNĐ (Mệnh giá lớn, không thập phân)
    @Min(value = 0, message = "Giá gốc không được âm")
    private BigDecimal originalPrice;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán không được âm")
    private BigDecimal salePrice;

    // Kho hàng (Dùng cho sách giấy)
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stock;

    // File tài sản kỹ thuật số
    private String coverObjectName; // Ảnh bìa riêng cho phiên bản này
    private String fileObjectName;  // File PDF/EPUB hoặc File nén

    // ==========================================
    // DỮ LIỆU ĐẶC THÙ CHO SÁCH NÓI (AUDIOBOOK)
    // ==========================================
    private Integer duration; // Tổng thời lượng
    
    private List<Long> narratorIds; // ID người đọc

    // Danh sách các chương sách nói (sử dụng @Valid để kiểm tra DTO con)
    @Valid 
    private List<AudioChapterCreateRequest> audioChapters;
}