package com.thientri.book_area.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thientri.book_area.dto.response.engagement.ReviewResponse;
import com.thientri.book_area.dto.response.engagement.UserLibraryResponse;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.engagement.Review;
import com.thientri.book_area.model.engagement.UserLibrary;
import com.thientri.book_area.service.minio.MinioService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EngagementMapper {

    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    public UserLibraryResponse toUserLibraryResponse(UserLibrary library, Integer listenProgress) {
        if (library == null || library.getEdition() == null) return null;
        
        BookEdition edition = library.getEdition();
        Book book = edition.getBook();
        String coverImage = resolveCoverImage(edition, book);
        
        return UserLibraryResponse.builder()
                .id(library.getId())
                .editionId(edition.getId())
                .bookTitle(book != null ? book.getTitle() : "N/A")
                .slug(book != null ? book.getSlug() : null)
                .authorName(book != null && !book.getAuthors().isEmpty()
                        ? book.getAuthors().iterator().next().getName() : "Chưa cập nhật tác giả")
                .format(edition.getFormat())
                .coverImageUrl(coverImage)
                .coverUrl(getCoverUrl(coverImage))
                .fileObjectName(edition.getFileObjectName()) // Link tải file hoặc manifest sách
                .currentListenProgress(listenProgress != null ? listenProgress : 0)
                .acquiredAt(library.getAcquiredAt())
                .build();
    }

    private String resolveCoverImage(BookEdition edition, Book book) {
        if (edition.getCoverObjectName() != null && !edition.getCoverObjectName().isBlank()) {
            return edition.getCoverObjectName();
        }
        if (edition.getCoverUrl() != null && !edition.getCoverUrl().isBlank()) {
            return edition.getCoverUrl();
        }
        if (book != null && book.getImages() != null && !book.getImages().isBlank()) {
            try {
                List<String> rawPaths = objectMapper.readValue(book.getImages(), new TypeReference<List<String>>() {});
                if (!rawPaths.isEmpty()) return rawPaths.get(0);
            } catch (Exception e) {
                return book.getImages();
            }
        }
        return null;
    }

    private String getCoverUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) return null;
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }
        try {
            return minioService.getPresignedUrl(objectName);
        } catch (RuntimeException exception) {
            return objectName;
        }
    }

    // ==========================================
    // MAPPER: Review -> ReviewResponse
    // ==========================================
    public ReviewResponse toReviewResponse(Review review) {
        if (review == null) return null;

        return ReviewResponse.builder()
                .id(review.getId())
                .reviewerName(review.getUser() != null ? review.getUser().getFullName() : "Người dùng ẩn danh")
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
