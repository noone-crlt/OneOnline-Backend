package com.thientri.book_area.service.catalog;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.thientri.book_area.dto.request.catalog.BookCreateRequest;
import com.thientri.book_area.dto.request.catalog.BookUpdateRequest;
import com.thientri.book_area.dto.response.catalog.BookDetailResponse;

public interface IBookService {

	BookDetailResponse getBookDetailBySlug(String slug);

	Page<BookDetailResponse> getAllBooks(Pageable pageable);

	// THÊM: Bắt buộc truyền danh sách file ảnh đính kèm
	void createBook(BookCreateRequest request, List<MultipartFile> imageFiles);

	void toggleBookActiveStatus(Long bookId, boolean isActive);

	// Sửa thông tin cơ bản của sách
	void updateBook(Long bookId, BookUpdateRequest request);

	// Quản lý ảnh của sách gốc
	
	
}
