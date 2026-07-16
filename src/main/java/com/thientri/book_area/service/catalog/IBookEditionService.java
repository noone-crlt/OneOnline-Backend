package com.thientri.book_area.service.catalog;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.thientri.book_area.dto.request.catalog.EditionCreateRequest;
import com.thientri.book_area.dto.request.catalog.EditionUpdateRequest;

public interface IBookEditionService {

	// Đã mở comment và khai báo đúng chuẩn nhận File từ Controller
	void createEdition(EditionCreateRequest request, MultipartFile coverFile, MultipartFile contentFile,
			List<MultipartFile> audioFiles);

	// Cập nhật giá và tồn kho
	// void updatePriceAndStock(String skuCode, BigDecimal newPrice, Integer
	// addedStock);

	// Cập nhật thông tin và thay thế file của phiên bản
	void updateEdition(Long editionId, EditionUpdateRequest request, MultipartFile newCoverFile,
			MultipartFile newContentFile);

	// Quản lý chương sách nói
	void deleteAudioChapter(Long chapterId);
}
