package com.thientri.book_area.controller.admin;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thientri.book_area.dto.request.catalog.EditionCreateRequest;
import com.thientri.book_area.dto.request.catalog.EditionUpdateRequest;
import com.thientri.book_area.service.catalog.IBookEditionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/editions")
@RequiredArgsConstructor
public class BookEditionController {

    private final IBookEditionService bookEditionService;

    // ==========================================
    // API ADMIN (Quản lý các định dạng Ebook/Audio/Physical)
    // ==========================================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createEdition(
            @Valid @RequestPart("data") EditionCreateRequest request,
            @RequestPart(value = "coverFile", required = false) MultipartFile coverFile,
            @RequestPart(value = "contentFile", required = false) MultipartFile contentFile,
            @RequestPart(value = "audioFiles", required = false) List<MultipartFile> audioFiles) {

        bookEditionService.createEdition(request, coverFile, contentFile, audioFiles);
        return ResponseEntity.ok("Tạo phiên bản sách thành công!");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateEdition(
            @PathVariable Long id,
            @Valid @RequestPart("data") EditionUpdateRequest request,
            @RequestPart(value = "newCoverFile", required = false) MultipartFile newCoverFile,
            @RequestPart(value = "newContentFile", required = false) MultipartFile newContentFile) {

        bookEditionService.updateEdition(id, request, newCoverFile, newContentFile);
        return ResponseEntity.ok("Cập nhật thông tin phiên bản thành công!");
    }

    @DeleteMapping("/audio-chapters/{chapterId}")
    public ResponseEntity<String> deleteAudioChapter(@PathVariable Long chapterId) {
        bookEditionService.deleteAudioChapter(chapterId);
        return ResponseEntity.ok("Xóa chương sách nói thành công!");
    }
}