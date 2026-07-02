package com.thientri.book_area.model.audio;

import com.thientri.book_area.model.catalog.BookEdition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "edition_audio_chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditionAudioChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "edition_id", nullable = false)
    private BookEdition edition;

    @Column(name = "title", length = 255)
    private String title;

    // THÊM MỚI: Bắt buộc có số chương để sắp xếp playlist
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    // Bắt buộc dùng NVARCHAR(MAX) cho đường dẫn file âm thanh
    @Column(name = "audio_file_name", columnDefinition = "NVARCHAR(MAX)")
    private String audioFileName;

    // Thời lượng tính bằng giây
    @Column(name = "duration")
    private Integer duration;
}