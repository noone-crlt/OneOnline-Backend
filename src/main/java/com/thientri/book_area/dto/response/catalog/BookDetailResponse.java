package com.thientri.book_area.dto.response.catalog;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailResponse {
	private Long id;
	private String title;
	private String slug;
	private String description;

	// FLATTENING TỐI ĐA: Thay vì trả về nguyên Object Publisher/Category, ta chỉ
	// trả về cái Frontend cần
	private String publisherName;
	private List<String> categoryNames;
	private List<String> authorNames;
	private List<String> imageUrls; // Chỉ lấy danh sách link ảnh

	// Danh sách các phiên bản (Sách giấy, Ebook, Audio) để Frontend làm nút bấm
	// chọn định dạng
	private List<EditionResponse> editions;
}
