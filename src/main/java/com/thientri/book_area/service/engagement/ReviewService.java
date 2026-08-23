package com.thientri.book_area.service.engagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.thientri.book_area.dto.request.engagement.ReviewRequest;
import com.thientri.book_area.dto.response.engagement.ReviewResponse;
import com.thientri.book_area.model.user.User;

public interface ReviewService {
	ReviewResponse createOrUpdateReview(User user, ReviewRequest request);

	Page<ReviewResponse> getApprovedReviewsByBook(Long bookId, Pageable pageable);
}
