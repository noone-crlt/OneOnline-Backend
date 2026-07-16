package com.thientri.book_area.repository.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.catalog.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	// Đảm bảo không tạo trùng tên danh mục
	Optional<Category> findByName(String name);
	boolean existsByName(String name);
}
