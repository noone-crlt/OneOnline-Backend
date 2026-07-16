package com.thientri.book_area.controller.engagement;

import org.springframework.data.domain.Page;
import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.engagement.UserLibraryResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.model.user.User;
import com.thientri.book_area.service.engagement.LibraryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {
	private final LibraryService libraryService;

	@GetMapping
	public ResponseEntity<ApiResponse<Page<UserLibraryResponse>>> getLibrary(@AuthenticationPrincipal User user,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(ApiResponse.success(libraryService.getLibrary(user, page, size)));
	}

	@PostMapping("/progress")
	public ResponseEntity<ApiResponse<Void>> saveProgress(@AuthenticationPrincipal User user,
			@RequestParam Long editionId, @RequestParam int progress) {
		libraryService.saveProgress(user, editionId, progress);
		return ResponseEntity.ok(ApiResponse.success("Đã lưu tiến độ", null));
	}
}
