package com.thientri.book_area.dto.request.catalog;

import java.util.List;
import lombok.Data;

@Data
public class BookUpdateRequest {
	private String title;
	private String slug;
	private String description;
	private Long publisherId;
	private List<Long> authorIds;
	private List<Long> categoryIds;
	private Boolean isActive;
}
