package com.thientri.book_area.controller.admin;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thientri.book_area.model.catalog.Category;
import com.thientri.book_area.repository.catalog.CategoryRepository;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.catalog.FeaturedCategoryResponse;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryRepository categoryRepository;

	@GetMapping
	public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
		return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAll()));
	}

	@GetMapping("/featured")
	public ResponseEntity<ApiResponse<List<FeaturedCategoryResponse>>> getFeaturedCategories() {
		return ResponseEntity.ok(ApiResponse.success(categoryRepository.findFeaturedCategories(PageRequest.of(0, 8))));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
		if (category.getName() == null || category.getName().isBlank()) {
			throw new BadRequestException("Tên danh mục không được để trống.");
		}
		if (categoryRepository.existsByName(category.getName().trim())) {
			throw new BadRequestException("Tên danh mục này đã tồn tại.");
		}
		category.setName(category.getName().trim());
		return ResponseEntity.ok(ApiResponse.success("Tạo danh mục thành công.", categoryRepository.save(category)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody Category categoryRequest) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục."));
		
		if (categoryRequest.getName() == null || categoryRequest.getName().isBlank()) {
			throw new BadRequestException("Tên danh mục không được để trống.");
		}
		
		String newName = categoryRequest.getName().trim();
		if (!category.getName().equals(newName) && categoryRepository.existsByName(newName)) {
			throw new BadRequestException("Tên danh mục này đã tồn tại.");
		}
		
		category.setName(newName);
		return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công.", categoryRepository.save(category)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục."));
		
		categoryRepository.delete(category);
		return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công.", null));
	}
}
