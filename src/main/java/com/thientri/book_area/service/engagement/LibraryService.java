package com.thientri.book_area.service.engagement;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.response.engagement.UserLibraryResponse;
import com.thientri.book_area.mapper.EngagementMapper;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.engagement.UserLibrary;
import com.thientri.book_area.model.order.Order;
import com.thientri.book_area.model.order.OrderItem;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.engagement.UserLibraryRepository;
import com.thientri.book_area.repository.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibraryService {
	private final UserLibraryRepository libraryRepository;
	private final OrderRepository orderRepository;
	private final EngagementMapper engagementMapper;

	@Transactional
	public Page<UserLibraryResponse> getLibrary(User user, int page, int size) {
		syncUserPurchasedBooks(user);
		return libraryRepository.findByUserIdOrderByAcquiredAtDesc(user.getId(), PageRequest.of(page, size))
				.map(item -> engagementMapper.toUserLibraryResponse(item,
						item.getProgress() != null ? item.getProgress() : 0));
	}

	private void syncUserPurchasedBooks(User user) {
		List<Order> validOrders = orderRepository.findByUserIdAndStatusIn(
				user.getId(), List.of("CONFIRMED", "SUCCESS", "DELIVERED", "COMPLETED"));
		for (Order order : validOrders) {
			if (order.getOrderItems() != null) {
				for (OrderItem item : order.getOrderItems()) {
					BookEdition edition = item.getEdition();
					if (edition != null && !libraryRepository.existsByUserIdAndEditionId(user.getId(), edition.getId())) {
						libraryRepository.save(UserLibrary.builder()
								.user(user)
								.edition(edition)
								.build());
					}
				}
			}
		}
	}

	@Transactional
	public void saveProgress(User user, Long editionId, int progress) {
		libraryRepository.findByUserIdAndEditionId(user.getId(), editionId).ifPresent(item -> {
			item.setProgress(progress);
			libraryRepository.save(item);
		});
	}
}
