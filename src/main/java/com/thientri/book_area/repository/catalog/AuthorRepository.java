package com.thientri.book_area.repository.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.dto.response.catalog.AuthorResponse;
import com.thientri.book_area.model.catalog.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
	Optional<Author> findByName(String name);
	boolean existsByName(String name);

	@Query("""
			SELECT new com.thientri.book_area.dto.response.catalog.AuthorResponse(
				a.id, a.name, a.bio, a.avatar, COUNT(b)
			)
			FROM Author a
			LEFT JOIN a.books b
			GROUP BY a.id, a.name, a.bio, a.avatar
			ORDER BY a.id ASC
			""")
	List<AuthorResponse> findAllWithBookCount();
}
