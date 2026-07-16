package com.thientri.book_area.dto.request.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AudioChapterCreateRequest {

	@NotNull(message = "Số chương không được để trống")
	@Min(value = 1, message = "Số chương phải từ 1 trở lên")
	private Integer chapterNumber;

	@NotBlank(message = "Tiêu đề chương không được để trống")
	private String title;

	@NotBlank(message = "Tên file âm thanh không được để trống")
	private String audioFileName;

	private Integer duration; // Thời lượng chương này
}
