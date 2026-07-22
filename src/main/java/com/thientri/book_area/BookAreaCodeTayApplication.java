package com.thientri.book_area;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BookAreaCodeTayApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookAreaCodeTayApplication.class, args);
	}

}
