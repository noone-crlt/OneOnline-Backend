package com.thientri.book_area.dto.response.engagement;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private String reviewerName; // Giấu email và ID của người đánh giá
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}