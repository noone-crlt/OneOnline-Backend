package com.thientri.book_area.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import com.thientri.book_area.dto.response.catalog.FeaturedCategoryResponse;
import com.thientri.book_area.repository.catalog.CategoryRepository;

import static org.mockito.Mockito.mock;

class CategoryControllerTest {

	@Test
	void getFeaturedCategoriesReturnsRepositoryResultsLimitedToEight() {
		CategoryRepository categoryRepository = mock(CategoryRepository.class);
		List<FeaturedCategoryResponse> featuredCategories = List.of(
				new FeaturedCategoryResponse(1L, "Văn học", 12L),
				new FeaturedCategoryResponse(2L, "Kinh tế", 9L));
		when(categoryRepository.findFeaturedCategories(any(Pageable.class))).thenReturn(featuredCategories);
		CategoryController controller = new CategoryController(categoryRepository);

		var response = controller.getFeaturedCategories();

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(categoryRepository).findFeaturedCategories(pageableCaptor.capture());
		assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(8);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getData()).containsExactlyElementsOf(featuredCategories);
	}
}
