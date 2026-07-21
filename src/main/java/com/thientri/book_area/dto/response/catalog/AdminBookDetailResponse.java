package com.thientri.book_area.dto.response.catalog;

import java.util.List;

public record AdminBookDetailResponse(
		Long id,
		String title,
		String slug,
		String description,
		Long publisherId,
		List<Long> authorIds,
		List<Long> categoryIds,
		List<String> imageUrls,
		boolean isActive) {
}
