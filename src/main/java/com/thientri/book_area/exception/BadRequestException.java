package com.thientri.book_area.exception;

public class BadRequestException extends RuntimeException {
	public BadRequestException(String message) {
		super(message);
	}
}
