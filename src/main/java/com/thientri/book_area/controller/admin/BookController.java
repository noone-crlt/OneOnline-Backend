package com.thientri.book_area.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thientri.book_area.dto.request.catalog.BookCreateRequest;
import com.thientri.book_area.dto.request.catalog.BookUpdateRequest;
import com.thientri.book_area.dto.response.catalog.BookDetailResponse;
import com.thientri.book_area.service.catalog.IBookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final IBookService bookService;

    // ==========================================
    // API PUBLIC (Frontend hiển thị)
    // ==========================================

    @GetMapping
    public ResponseEntity<Page<BookDetailResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Mặc định sắp xếp sách mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BookDetailResponse> getBookDetail(@PathVariable String slug) {
        return ResponseEntity.ok(bookService.getBookDetailBySlug(slug));
    }

    // ==========================================
    // API ADMIN (Quản lý dữ liệu)
    // ==========================================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createBook(
            @Valid @RequestPart("data") BookCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {

        bookService.createBook(request, imageFiles);
        return ResponseEntity.ok("Tạo sách thành công!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {

        bookService.updateBook(id, request);
        return ResponseEntity.ok("Cập nhật sách thành công!");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {

        bookService.toggleBookActiveStatus(id, isActive);
        return ResponseEntity.ok("Đã thay đổi trạng thái kinh doanh của sách!");
    }

    // Quản lý hình ảnh độc lập
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addBookImages(
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> imageFiles) {

        bookService.addBookImages(id, imageFiles);
        return ResponseEntity.ok("Thêm ảnh thành công!");
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<String> deleteBookImage(@PathVariable Long imageId) {
        bookService.deleteBookImage(imageId);
        return ResponseEntity.ok("Xóa ảnh thành công!");
    }
}