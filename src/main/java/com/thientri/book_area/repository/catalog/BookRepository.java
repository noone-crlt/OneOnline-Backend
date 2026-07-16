package com.thientri.book_area.repository.catalog;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.catalog.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

	// Tìm sách theo đường dẫn SEO (Ví dụ: /books/dac-nhan-tam)
	Optional<Book> findBySlug(String slug);

	boolean existsBySlug(String slug);

	long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
