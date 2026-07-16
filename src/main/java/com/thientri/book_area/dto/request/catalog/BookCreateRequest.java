package com.thientri.book_area.dto.request.catalog;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookCreateRequest {

	@NotBlank(message = "Tên sách không được để trống")
	private String title;

	// Slug có thể tự gen từ title ở tầng Service, nhưng cho phép Admin tự nhập để
	// tối ưu SEO
	@NotBlank(message = "Đường dẫn SEO (slug) không được để trống")
	private String slug;

	private String description;

	@NotNull(message = "Vui lòng chọn nhà xuất bản")
	private Long publisherId;

	// Danh sách ID Tác giả (Bắt buộc phải có ít nhất 1)
	@NotEmpty(message = "Vui lòng chọn ít nhất 1 tác giả")
	private List<Long> authorIds;

	// Danh sách ID Danh mục (Bắt buộc phải có ít nhất 1)
	@NotEmpty(message = "Vui lòng chọn ít nhất 1 danh mục")
	private List<Long> categoryIds;

	// Danh sách link ảnh tổng quan của sách (có thể trống)
	private List<String> imageUrls;

	private Boolean isActive = true;
}
