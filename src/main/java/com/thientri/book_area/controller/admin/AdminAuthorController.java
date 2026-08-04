package com.thientri.book_area.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thientri.book_area.dto.request.catalog.AuthorRequest;
import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.catalog.AuthorResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.model.catalog.Author;
import com.thientri.book_area.repository.catalog.AuthorRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

	private final AuthorRepository authorRepository;

	@GetMapping
	public ResponseEntity<ApiResponse<List<AuthorResponse>>> getAllAuthors() {
		List<Object[]> rows = authorRepository.findAllWithBookCountRaw();
		List<AuthorResponse> result = rows.stream().map(row -> AuthorResponse.builder()
				.id(((Number) row[0]).longValue())
				.name((String) row[1])
				.bio((String) row[2])
				.avatar((String) row[3])
				.bookCount(((Number) row[4]).longValue())
				.build()).toList();
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<Author>> createAuthor(@RequestBody AuthorRequest request) {
		if (request.getName() == null || request.getName().isBlank()) {
			throw new BadRequestException("Tên tác giả không được để trống.");
		}
		String name = request.getName().trim();
		if (authorRepository.existsByName(name)) {
			throw new BadRequestException("Tên tác giả này đã tồn tại.");
		}

		Author author = Author.builder()
				.name(name)
				.bio(request.getBio() != null ? request.getBio().trim() : null)
				.avatar(request.getAvatar() != null ? request.getAvatar().trim() : null)
				.build();

		return ResponseEntity.ok(ApiResponse.success("Tạo tác giả thành công.", authorRepository.save(author)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Author>> updateAuthor(@PathVariable Long id, @RequestBody AuthorRequest request) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả."));

		if (request.getName() == null || request.getName().isBlank()) {
			throw new BadRequestException("Tên tác giả không được để trống.");
		}

		String newName = request.getName().trim();
		if (!author.getName().equalsIgnoreCase(newName) && authorRepository.existsByName(newName)) {
			throw new BadRequestException("Tên tác giả này đã tồn tại.");
		}

		author.setName(newName);
		if (request.getBio() != null) author.setBio(request.getBio().trim());
		if (request.getAvatar() != null) author.setAvatar(request.getAvatar().trim());

		return ResponseEntity.ok(ApiResponse.success("Cập nhật tác giả thành công.", authorRepository.save(author)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteAuthor(@PathVariable Long id) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả."));

		authorRepository.delete(author);
		return ResponseEntity.ok(ApiResponse.success("Xóa tác giả thành công.", null));
	}
}
