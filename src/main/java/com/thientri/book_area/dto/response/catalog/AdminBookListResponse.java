package com.thientri.book_area.dto.response.catalog;

import java.util.List;

public record AdminBookListResponse(
		Long id,
		String title,
		String slug,
		String publisherName,
		List<String> authorNames,
		List<String> categoryNames,
		List<String> imageUrls,
		boolean isActive) {
}
