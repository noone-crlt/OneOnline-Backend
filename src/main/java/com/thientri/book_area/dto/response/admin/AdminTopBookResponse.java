package com.thientri.book_area.dto.response.admin;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTopBookResponse {
	private Long bookId;
	private String title;
	private String slug;
	private String coverUrl;
	private String authorName;
	private String categoryName;
	private Long totalSold;
	private BigDecimal totalRevenue;
}
