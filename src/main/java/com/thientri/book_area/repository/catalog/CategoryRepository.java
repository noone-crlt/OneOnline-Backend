package com.thientri.book_area.repository.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.dto.response.catalog.FeaturedCategoryResponse;
import com.thientri.book_area.model.catalog.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	// Đảm bảo không tạo trùng tên danh mục
	Optional<Category> findByName(String name);
	boolean existsByName(String name);

	@Query("""
			SELECT new com.thientri.book_area.dto.response.catalog.FeaturedCategoryResponse(
				category.id, category.name, COUNT(book)
			)
			FROM Book book
			JOIN book.categories category
			WHERE book.isActive = true
			GROUP BY category.id, category.name
			ORDER BY COUNT(book) DESC, category.name ASC
			""")
	List<FeaturedCategoryResponse> findFeaturedCategories(Pageable pageable);
}
