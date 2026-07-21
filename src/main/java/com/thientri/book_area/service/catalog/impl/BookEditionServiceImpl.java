package com.thientri.book_area.service.catalog.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.thientri.book_area.dto.request.catalog.AudioChapterCreateRequest;
import com.thientri.book_area.dto.request.catalog.EditionCreateRequest;
import com.thientri.book_area.dto.request.catalog.EditionUpdateRequest;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.model.audio.EditionAudioChapter;
import com.thientri.book_area.model.audio.Narrator;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.repository.catalog.BookEditionRepository;
import com.thientri.book_area.repository.catalog.BookRepository;
import com.thientri.book_area.repository.audio.EditionAudioChapterRepository;
import com.thientri.book_area.repository.audio.NarratorRepository;
import com.thientri.book_area.service.catalog.IBookEditionService;
import com.thientri.book_area.service.minio.MinioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookEditionServiceImpl implements IBookEditionService {

    private final BookRepository bookRepository;
    private final BookEditionRepository bookEditionRepository;
    private final EditionAudioChapterRepository audioChapterRepository;
    private final NarratorRepository narratorRepository;
    private final MinioService minioService;

    @Override
    @Transactional
    public void createEdition(EditionCreateRequest request, 
                              MultipartFile coverFile, 
                              MultipartFile contentFile, 
                              List<MultipartFile> audioFiles) {
        
        List<String> uploadedObjects = new ArrayList<>();
        try {
            // 1. Kiểm tra sách gốc và mã SKU
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy sách gốc với ID: " + request.getBookId()));

            if (bookEditionRepository.findBySkuCode(request.getSkuCode()).isPresent()) {
                throw new BadRequestException("Mã SKU này đã tồn tại trong hệ thống.");
            }

            // 2. Cross-field Validation (Kiểm tra chéo nghiệp vụ khắt khe)
            validateFormatLogic(request, contentFile, audioFiles);

            // 3. Xử lý Upload Ảnh Bìa (Dùng chung cho mọi định dạng nếu có)
            String coverObjectName = null;
            if (coverFile != null && !coverFile.isEmpty()) {
                coverObjectName = minioService.uploadFile(coverFile, "sach/anhbia");
                uploadedObjects.add(coverObjectName);
            }

            // 4. Khởi tạo Entity Phiên bản (Edition)
            BookEdition newEdition = BookEdition.builder()
                    .book(book)
                    .format(request.getFormat())
                    .skuCode(request.getSkuCode())
                    .originalPrice(request.getOriginalPrice())
                    .salePrice(request.getSalePrice())
                    .stock(request.getFormat().equals("PHYSICAL") ? request.getStock() : null)
                    .coverObjectName(coverObjectName)
                    .duration(request.getDuration())
                    .build();

            // 5. Xử lý logic riêng cho từng định dạng
            switch (request.getFormat()) {
                case "EBOOK_PDF":
                case "EBOOK_EPUB":
                    // Upload file mềm
                    String fileObjectName = minioService.uploadFile(contentFile, "ebooks");
                    uploadedObjects.add(fileObjectName);
                    newEdition.setFileObjectName(fileObjectName);
                    break;

                case "AUDIOBOOK":
                    // Map Người đọc (Narrators)
                    if (request.getNarratorIds() != null && !request.getNarratorIds().isEmpty()) {
                        List<Narrator> narrators = narratorRepository.findAllById(request.getNarratorIds());
                        newEdition.setNarrators(new HashSet<>(narrators));
                    }

                    // Xử lý Upload các chương âm thanh
                    List<EditionAudioChapter> chapters = new ArrayList<>();
                    for (int i = 0; i < request.getAudioChapters().size(); i++) {
                        AudioChapterCreateRequest chapterDTO = request.getAudioChapters().get(i);
                        MultipartFile audioFile = audioFiles.get(i); // Giả định frontend gửi mảng file khớp thứ tự DTO

                        String audioFileName = minioService.uploadFile(audioFile, "audiobooks");
                        uploadedObjects.add(audioFileName);

                        EditionAudioChapter chapter = EditionAudioChapter.builder()
                                .edition(newEdition)
                                .chapterNumber(chapterDTO.getChapterNumber())
                                .title(chapterDTO.getTitle())
                                .audioFileName(audioFileName)
                                .duration(chapterDTO.getDuration())
                                .build();
                        chapters.add(chapter);
                    }
                    newEdition.setAudioChapters(chapters);
                    break;
            }

            // 6. Lưu xuống Database (CascadeType.ALL sẽ tự động lưu luôn các AudioChapter)
            bookEditionRepository.save(newEdition);
            log.info("Tạo phiên bản sách thành công: SKU {}", request.getSkuCode());
        } catch (Exception e) {
            log.error("Lỗi khi tạo phiên bản sách, bắt đầu dọn dẹp các file đã upload lên MinIO: ", e);
            for (String objectName : uploadedObjects) {
                try {
                    minioService.deleteFile(objectName);
                } catch (Exception ex) {
                    log.error("Không thể dọn dẹp file {} sau lỗi: ", objectName, ex);
                }
            }
            throw e;
        }
    }

    // =========================================================
    // HELPER: Tách riêng logic kiểm tra để hàm main không bị rối
    // =========================================================
    private void validateFormatLogic(EditionCreateRequest request, MultipartFile contentFile, List<MultipartFile> audioFiles) {
        String format = request.getFormat();

        if (format.equals("PHYSICAL")) {
            if (request.getStock() == null) {
                throw new BadRequestException("Sách giấy bắt buộc phải nhập số lượng tồn kho.");
            }
        } 
        else if (format.equals("EBOOK_PDF") || format.equals("EBOOK_EPUB")) {
            if (contentFile == null || contentFile.isEmpty()) {
                throw new BadRequestException("Ebook bắt buộc phải đính kèm file nội dung (PDF/EPUB).");
            }
        } 
        else if (format.equals("AUDIOBOOK")) {
            if (request.getAudioChapters() == null || request.getAudioChapters().isEmpty()) {
                throw new BadRequestException("Sách nói bắt buộc phải có thông tin các chương.");
            }
            if (audioFiles == null || audioFiles.size() != request.getAudioChapters().size()) {
                throw new BadRequestException("Số lượng file âm thanh tải lên không khớp với số lượng chương khai báo.");
            }
        }
    }

    @Override
    @Transactional
    public void updateEdition(Long editionId, EditionUpdateRequest request, 
                              MultipartFile newCoverFile, MultipartFile newContentFile) {
        
        BookEdition edition = bookEditionRepository.findById(editionId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phiên bản sách."));

        // Cập nhật giá và kho
        if (request.getOriginalPrice() != null) edition.setOriginalPrice(request.getOriginalPrice());
        if (request.getSalePrice() != null) edition.setSalePrice(request.getSalePrice());
        if (request.getStock() != null) edition.setStock(request.getStock());
        if (request.getDuration() != null) edition.setDuration(request.getDuration());

        if (request.getNarratorIds() != null && edition.getFormat().equals("AUDIOBOOK")) {
            edition.setNarrators(new HashSet<>(narratorRepository.findAllById(request.getNarratorIds())));
        }

        List<String> uploadedObjects = new ArrayList<>();
        String oldCoverToDelete = null;
        String oldContentToDelete = null;

        try {
            // THAY THẾ ẢNH BÌA: Xóa ảnh cũ -> Upload ảnh mới
            if (newCoverFile != null && !newCoverFile.isEmpty()) {
                if (edition.getCoverObjectName() != null) {
                    oldCoverToDelete = edition.getCoverObjectName();
                }
                String newCover = minioService.uploadFile(newCoverFile, "sach/anhbia");
                uploadedObjects.add(newCover);
                edition.setCoverObjectName(newCover);
            }

            // THAY THẾ FILE NỘI DUNG (PDF/EPUB)
            if (newContentFile != null && !newContentFile.isEmpty()) {
                if (!edition.getFormat().startsWith("EBOOK")) {
                    throw new BadRequestException("Chỉ Ebook mới có thể cập nhật file nội dung.");
                }
                if (edition.getFileObjectName() != null) {
                    oldContentToDelete = edition.getFileObjectName();
                }
                String newContent = minioService.uploadFile(newContentFile, "ebooks");
                uploadedObjects.add(newContent);
                edition.setFileObjectName(newContent);
            }

            bookEditionRepository.save(edition);

            // Dọn dẹp các file cũ thực tế sau khi DB save thành công
            if (oldCoverToDelete != null) {
                try {
                    minioService.deleteFile(oldCoverToDelete);
                } catch (Exception e) {
                    log.error("Lỗi khi xóa ảnh bìa cũ {}", oldCoverToDelete, e);
                }
            }
            if (oldContentToDelete != null) {
                try {
                    minioService.deleteFile(oldContentToDelete);
                } catch (Exception e) {
                    log.error("Lỗi khi xóa file nội dung cũ {}", oldContentToDelete, e);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật phiên bản sách, dọn dẹp các file mới vừa upload lên MinIO: ", e);
            for (String objectName : uploadedObjects) {
                try {
                    minioService.deleteFile(objectName);
                } catch (Exception ex) {
                    log.error("Không thể dọn dẹp file {} sau lỗi: ", objectName, ex);
                }
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteAudioChapter(Long chapterId) {
        EditionAudioChapter chapter = audioChapterRepository.findById(chapterId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chương sách nói."));
        
        // 1. Xóa file âm thanh (.mp3) khỏi MinIO để giải phóng dung lượng
        if (chapter.getAudioFileName() != null) {
            minioService.deleteFile(chapter.getAudioFileName());
        }
        
        // 2. Xóa khỏi Database
        audioChapterRepository.delete(chapter);
    }
}