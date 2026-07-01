package com.thientri.book_area.mapper;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.thientri.book_area.dto.response.catalog.AudioChapterResponse;
import com.thientri.book_area.dto.response.catalog.BookDetailResponse;
import com.thientri.book_area.dto.response.catalog.EditionResponse;
import com.thientri.book_area.model.audio.EditionAudioChapter;
import com.thientri.book_area.model.audio.Narrator;
import com.thientri.book_area.model.catalog.Author;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.catalog.BookImage;
import com.thientri.book_area.model.catalog.Category;

@Component
public class CatalogMapper {

    // ==========================================
    // 1. MAPPER: Sách Gốc -> BookDetailResponse
    // ==========================================
    public BookDetailResponse toBookDetailResponse(Book book) {
        if (book == null) {
            return null;
        }

        return BookDetailResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .description(book.getDescription())
                
                // Flattening: Chỉ lấy tên nhà xuất bản an toàn (chống NullPointerException)
                .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : null)
                
                // Flattening Collections: Biến Set<Object> thành List<String>
                .categoryNames(book.getCategories() == null ? Collections.emptyList() : 
                        book.getCategories().stream().map(Category::getName).collect(Collectors.toList()))
                        
                .authorNames(book.getAuthors() == null ? Collections.emptyList() : 
                        book.getAuthors().stream().map(Author::getName).collect(Collectors.toList()))
                        
                .imageUrls(book.getImages() == null ? Collections.emptyList() : 
                        book.getImages().stream().map(BookImage::getImageFileName).collect(Collectors.toList()))
                        
                // Mapping danh sách các phiên bản (Variants)
                .editions(book.getEditions() == null ? Collections.emptyList() : 
                        book.getEditions().stream().map(this::toEditionResponse).collect(Collectors.toList()))
                        
                .build();
    }

    // ==========================================
    // 2. MAPPER: Phiên bản sách -> EditionResponse
    // ==========================================
    public EditionResponse toEditionResponse(BookEdition edition) {
        if (edition == null) {
            return null;
        }

        EditionResponse.EditionResponseBuilder builder = EditionResponse.builder()
                .id(edition.getId())
                .format(edition.getFormat())
                .skuCode(edition.getSkuCode())
                .originalPrice(edition.getOriginalPrice())
                .salePrice(edition.getSalePrice())
                .stock(edition.getStock())
                .coverObjectName(edition.getCoverObjectName())
                .fileObjectName(edition.getFileObjectName())
                .duration(edition.getDuration());

        // Flattening: Chỉ lấy tên người đọc nếu có
        if (edition.getNarrators() != null && !edition.getNarrators().isEmpty()) {
            builder.narratorNames(edition.getNarrators().stream()
                    .map(Narrator::getName)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    // ==========================================
    // 3. MAPPER: Chương sách nói -> AudioChapterResponse
    // ==========================================
    public AudioChapterResponse toAudioChapterResponse(EditionAudioChapter chapter) {
        if (chapter == null) {
            return null;
        }

        return AudioChapterResponse.builder()
                .id(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .audioFileName(chapter.getAudioFileName())
                .duration(chapter.getDuration())
                .build();
    }
}