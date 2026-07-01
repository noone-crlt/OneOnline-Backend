package com.thientri.book_area.dto.response.catalog;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditionResponse {
    private Long id;
    private String format; // 'PHYSICAL', 'EBOOK_PDF', 'AUDIOBOOK'
    private String skuCode;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private String coverObjectName;
    private String fileObjectName; 
    private Integer duration; // Tổng thời lượng (nếu có)
    
    // Nếu là sách nói, trả kèm danh sách chương đã sắp xếp
    private List<AudioChapterResponse> audioChapters;
    
    // Flattening: Gộp tên những người đọc sách nói thành 1 mảng String
    private List<String> narratorNames; 
}