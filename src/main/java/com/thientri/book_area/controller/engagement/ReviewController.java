package com.thientri.book_area.controller.engagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.dto.request.engagement.ReviewRequest;
import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.engagement.ReviewResponse;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.service.engagement.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody ReviewRequest request) {
		ReviewResponse response = reviewService.createOrUpdateReview(user, request);
		return ResponseEntity.ok(ApiResponse.success("Đánh giá tác phẩm thành công!", response));
	}

	@GetMapping("/book/{bookId}")
	public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByBook(
			@PathVariable Long bookId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ReviewResponse> reviews = reviewService.getApprovedReviewsByBook(bookId, pageable);
		return ResponseEntity.ok(ApiResponse.success(reviews));
	}
}
