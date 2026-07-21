package com.thientri.book_area.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.thientri.book_area.dto.response.catalog.AdminBookListResponse;
import com.thientri.book_area.service.catalog.IBookService;

class AdminBookManagementControllerTest {

	@Test
	void listsBooksWithRequestedFiltersAndBoundedPageSize() {
		IBookService bookService = mock(IBookService.class);
		when(bookService.getAdminBooks(any(), any(), any(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(new AdminBookListResponse(1L, "Sách", "sach", null,
						List.of(), List.of(), List.of(), true))));
		AdminBookManagementController controller = new AdminBookManagementController(bookService);

		var response = controller.getBooks(2, 500, "tìm", "Văn học", true);

		ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(bookService).getAdminBooks(any(), any(), any(), pageable.capture());
		assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
		assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
		assertThat(response.getBody().getData().getContent()).hasSize(1);
	}
}
