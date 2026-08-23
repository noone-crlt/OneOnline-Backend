package com.thientri.book_area.service.engagement;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.request.engagement.ReviewRequest;
import com.thientri.book_area.dto.response.engagement.ReviewResponse;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.mapper.EngagementMapper;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.engagement.Review;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.catalog.BookRepository;
import com.thientri.book_area.repository.engagement.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final ReviewRepository reviewRepository;
	private final BookRepository bookRepository;
	private final EngagementMapper engagementMapper;

	@Override
	@Transactional
	public ReviewResponse createOrUpdateReview(User user, ReviewRequest request) {
		Book book = bookRepository.findById(request.getBookId())
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác phẩm với ID: " + request.getBookId()));

		Optional<Review> existingReviewOpt = reviewRepository.findByUserIdAndBookId(user.getId(), book.getId());

		Review review;
		if (existingReviewOpt.isPresent()) {
			review = existingReviewOpt.get();
			review.setRating(request.getRating());
			review.setComment(request.getComment());
			review.setIsApproved(true);
		} else {
			review = Review.builder()
					.user(user)
					.book(book)
					.rating(request.getRating())
					.comment(request.getComment())
					.isApproved(true)
					.build();
		}

		Review saved = reviewRepository.save(review);
		return engagementMapper.toReviewResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ReviewResponse> getApprovedReviewsByBook(Long bookId, Pageable pageable) {
		return reviewRepository.findByBookIdAndIsApprovedTrueOrderByCreatedAtDesc(bookId, pageable)
				.map(engagementMapper::toReviewResponse);
	}
}
