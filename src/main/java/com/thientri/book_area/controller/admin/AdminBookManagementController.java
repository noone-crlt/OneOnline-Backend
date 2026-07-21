package com.thientri.book_area.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thientri.book_area.dto.request.catalog.BookCreateRequest;
import com.thientri.book_area.dto.request.catalog.BookUpdateRequest;
import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.catalog.AdminBookDetailResponse;
import com.thientri.book_area.dto.response.catalog.AdminBookListResponse;
import com.thientri.book_area.dto.response.catalog.BookFormOptionsResponse;
import com.thientri.book_area.service.catalog.IBookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class AdminBookManagementController {
	private final IBookService bookService;

	@GetMapping
	public ResponseEntity<ApiResponse<Page<AdminBookListResponse>>> getBooks(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) Boolean isActive) {
		Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by("createdAt").descending());
		return ResponseEntity.ok(ApiResponse.success(bookService.getAdminBooks(q, category, isActive, pageable)));
	}

	@GetMapping("/form-options")
	public ResponseEntity<ApiResponse<BookFormOptionsResponse>> getFormOptions() {
		return ResponseEntity.ok(ApiResponse.success(bookService.getBookFormOptions()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminBookDetailResponse>> getBook(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(bookService.getAdminBook(id)));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Void>> createBook(@Valid @RequestPart("data") BookCreateRequest request,
			@RequestPart(value = "coverFile", required = false) MultipartFile coverFile) {
		bookService.createBook(request, coverFile);
		return ResponseEntity.ok(ApiResponse.success("Tạo sách thành công.", null));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Void>> updateBook(@PathVariable Long id,
			@Valid @RequestPart("data") BookUpdateRequest request,
			@RequestPart(value = "coverFile", required = false) MultipartFile coverFile) {
		bookService.updateBook(id, request, coverFile);
		return ResponseEntity.ok(ApiResponse.success("Cập nhật sách thành công.", null));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long id, @RequestParam boolean isActive) {
		bookService.toggleBookActiveStatus(id, isActive);
		return ResponseEntity.ok(ApiResponse.success(isActive ? "Đã hiển thị sách." : "Đã ẩn sách.", null));
	}
}
