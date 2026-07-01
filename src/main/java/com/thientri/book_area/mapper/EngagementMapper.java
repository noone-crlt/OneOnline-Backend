package com.thientri.book_area.mapper;

import org.springframework.stereotype.Component;

import com.thientri.book_area.dto.response.engagement.ReviewResponse;
import com.thientri.book_area.dto.response.engagement.UserLibraryResponse;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.engagement.Review;
import com.thientri.book_area.model.engagement.UserLibrary;

@Component
public class EngagementMapper {

    // Mapper này phải nhận thêm tham số progress (Tiến độ nghe) nếu có
    public UserLibraryResponse toUserLibraryResponse(UserLibrary library, Integer listenProgress) {
        if (library == null || library.getEdition() == null) return null;
        
        BookEdition edition = library.getEdition();
        
        return UserLibraryResponse.builder()
                .id(library.getId())
                .editionId(edition.getId())
                .bookTitle(edition.getBook() != null ? edition.getBook().getTitle() : "N/A")
                .format(edition.getFormat())
                .coverImageUrl(edition.getCoverObjectName())
                .fileObjectName(edition.getFileObjectName()) // Link tải file hoặc manifest sách
                .currentListenProgress(listenProgress != null ? listenProgress : 0)
                .acquiredAt(library.getAcquiredAt())
                .build();
    }

    // ==========================================
    // MAPPER: Review -> ReviewResponse
    // ==========================================
    public ReviewResponse toReviewResponse(Review review) {
        
        if (review == null) return null;

        return com.thientri.book_area.dto.response.engagement.ReviewResponse.builder()
                .id(review.getId())
                .reviewerName(review.getUser() != null ? review.getUser().getFullName() : "Người dùng ẩn danh")
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}