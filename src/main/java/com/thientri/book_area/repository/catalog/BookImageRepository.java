package com.thientri.book_area.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thientri.book_area.model.catalog.BookImage;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {

}
