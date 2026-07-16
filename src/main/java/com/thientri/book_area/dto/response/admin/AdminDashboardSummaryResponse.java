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
public class AdminDashboardSummaryResponse {
	private long totalUsers;
	private long totalBooks;
	private long totalCategories;
	private long totalAuthors;
	private long totalChapters;
	private long totalComments;
	private BigDecimal totalRevenue;
}
