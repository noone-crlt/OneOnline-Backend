package com.thientri.book_area.controller.admin;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.model.catalog.Category;
import com.thientri.book_area.repository.catalog.CategoryRepository;

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
}
