package com.thientri.book_area.dto.response.admin;

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
public class AdminMonthlyStatsResponse {
	private String month;
	private long newUsers;
	private long newBooks;
	private long comments;
	private java.math.BigDecimal revenue;
}
