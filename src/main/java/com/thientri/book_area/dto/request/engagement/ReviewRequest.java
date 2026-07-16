package com.thientri.book_area.dto.request.engagement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
	@NotNull(message = "ID Sách không được trống")
	private Long bookId;

	@NotNull(message = "Điểm đánh giá không được trống")
	@Min(value = 1, message = "Điểm thấp nhất là 1")
	@Max(value = 5, message = "Điểm cao nhất là 5")
	private Integer rating;

	private String comment;
}
