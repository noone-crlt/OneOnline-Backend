package com.thientri.book_area.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {

	@Test
	void returnsBadRequestForMalformedJson() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		var response = handler.handleUnreadableRequest(
				new HttpMessageNotReadableException("Invalid JSON"), mock(HttpServletRequest.class));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isEqualTo("Dữ liệu gửi lên không đúng định dạng.");
	}
}
