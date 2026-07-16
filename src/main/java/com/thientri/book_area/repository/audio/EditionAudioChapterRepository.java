package com.thientri.book_area.repository.audio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.audio.EditionAudioChapter;

@Repository
public interface EditionAudioChapterRepository extends JpaRepository<EditionAudioChapter, Long> {
	// Lấy danh sách chương của 1 sách nói, bắt buộc phải sắp xếp tăng dần theo số
	// chương
	List<EditionAudioChapter> findByEditionIdOrderByChapterNumberAsc(Long editionId);
}
