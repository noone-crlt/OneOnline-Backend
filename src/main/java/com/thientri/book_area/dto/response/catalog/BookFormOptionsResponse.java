package com.thientri.book_area.dto.response.catalog;

import java.util.List;

public record BookFormOptionsResponse(
		List<CatalogOptionResponse> publishers,
		List<CatalogOptionResponse> authors,
		List<CatalogOptionResponse> categories) {
}
