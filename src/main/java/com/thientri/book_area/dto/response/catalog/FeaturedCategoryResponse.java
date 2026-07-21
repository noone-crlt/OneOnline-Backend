package com.thientri.book_area.dto.response.catalog;

/**
 * A category ranked by the number of active books assigned to it.
 */
public record FeaturedCategoryResponse(Long id, String name, long bookCount) {
}
