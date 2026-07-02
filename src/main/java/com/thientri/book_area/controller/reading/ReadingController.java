package com.thientri.book_area.controller.reading;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.exception.ForbiddenException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.catalog.BookEditionRepository;
import com.thientri.book_area.repository.engagement.UserLibraryRepository;
import com.thientri.book_area.service.minio.MinioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reading")
@RequiredArgsConstructor
public class ReadingController {
    private final BookEditionRepository editionRepository;
    private final UserLibraryRepository libraryRepository;
    private final MinioService minioService;

    @GetMapping("/{editionId}/url")
    public ResponseEntity<Map<String, String>> getReadingUrl(@AuthenticationPrincipal User user,
            @PathVariable Long editionId) {
        BookEdition edition = editionRepository.findById(editionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên bản sách."));
        if (!libraryRepository.existsByUserIdAndEditionId(user.getId(), editionId)) {
            throw new ForbiddenException("Bạn cần mua sách trước khi đọc nội dung đầy đủ.");
        }
        if (edition.getFileObjectName() == null || edition.getFileObjectName().isBlank()) {
            throw new ResourceNotFoundException("Phiên bản sách chưa có file nội dung.");
        }
        return ResponseEntity.ok(Map.of("url", minioService.getPresignedUrl(edition.getFileObjectName())));
    }
}
