package com.thientri.book_area.dto.response.engagement;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLibraryResponse {
	private Long id;
	private Long editionId;

	// Dữ liệu làm phẳng từ Book và BookEdition
	private String bookTitle;
	private String slug;
	private String authorName;
	private String format; // EBOOK_PDF, AUDIOBOOK
	private String coverImageUrl;
	private String coverUrl;

	// ĐƯỜNG DẪN TÀI SẢN KỸ THUẬT SỐ (Cốt lõi của bảng này)
	private String fileObjectName; // Dùng để gọi MinIO lấy file PDF/EPUB

	// Nếu là sách nói, lấy tiến độ nghe cuối cùng để phát tiếp
	private Integer currentListenProgress;

	private LocalDateTime acquiredAt;
}
