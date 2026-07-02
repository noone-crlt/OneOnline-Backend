package com.thientri.book_area.dto.response.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioChapterResponse {
    private Long id;
    private Integer chapterNumber;
    private String title;
    private String audioFileName;
    private Integer duration;
}