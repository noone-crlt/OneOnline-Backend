package com.thientri.book_area.repository.catalog;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.catalog.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

	// Tìm sách theo đường dẫn SEO (Ví dụ: /books/dac-nhan-tam)
	Optional<Book> findBySlug(String slug);

	boolean existsBySlug(String slug);

	@Query("""
			SELECT DISTINCT book
			FROM Book book
			LEFT JOIN book.authors author
			LEFT JOIN book.categories category
			LEFT JOIN book.editions edition
			LEFT JOIN book.publisher publisher
			WHERE book.isActive = true
			  AND (
			      :search IS NULL
			      OR LOWER(book.title) LIKE LOWER(CONCAT('%', :search, '%'))
			      OR LOWER(book.description) LIKE LOWER(CONCAT('%', :search, '%'))
			      OR LOWER(author.name) LIKE LOWER(CONCAT('%', :search, '%'))
			      OR LOWER(publisher.name) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			  AND (:category IS NULL OR LOWER(category.name) = LOWER(:category))
			  AND (
			      :format IS NULL
			      OR edition.format = :format
			      OR (:format = 'EBOOK' AND edition.format IN ('EBOOK_PDF', 'EBOOK_EPUB'))
			  )
			""")
	Page<Book> findCatalog(@Param("search") String search,
			@Param("category") String category,
			@Param("format") String format,
			Pageable pageable);

	@Query(value = """
			SELECT DISTINCT book
			FROM Book book
			LEFT JOIN book.authors author
			LEFT JOIN book.categories category
			WHERE (:search IS NULL
				OR LOWER(book.title) LIKE LOWER(CONCAT('%', :search, '%'))
				OR LOWER(author.name) LIKE LOWER(CONCAT('%', :search, '%')))
			  AND (:category IS NULL OR LOWER(category.name) = LOWER(:category))
			  AND (:isActive IS NULL OR book.isActive = :isActive)
			""", countQuery = """
			SELECT COUNT(DISTINCT book)
			FROM Book book
			LEFT JOIN book.authors author
			LEFT JOIN book.categories category
			WHERE (:search IS NULL
				OR LOWER(book.title) LIKE LOWER(CONCAT('%', :search, '%'))
				OR LOWER(author.name) LIKE LOWER(CONCAT('%', :search, '%')))
			  AND (:category IS NULL OR LOWER(category.name) = LOWER(:category))
			  AND (:isActive IS NULL OR book.isActive = :isActive)
			""")
	Page<Book> findAdminCatalog(@Param("search") String search,
			@Param("category") String category,
			@Param("isActive") Boolean isActive,
			Pageable pageable);

	long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
