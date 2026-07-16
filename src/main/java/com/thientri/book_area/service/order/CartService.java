package com.thientri.book_area.service.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.request.order.AddToCartRequest;
import com.thientri.book_area.dto.response.order.CartResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.mapper.OrderMapper;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.order.CartItem;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.catalog.BookEditionRepository;
import com.thientri.book_area.repository.order.CartItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
	private final CartItemRepository cartItemRepository;
	private final BookEditionRepository editionRepository;
	private final OrderMapper orderMapper;

	@Transactional(readOnly = true)
	public CartResponse getCart(User user) {
		return orderMapper.toCartResponse(cartItemRepository.findByUserId(user.getId()));
	}

	@Transactional
	public CartResponse addItem(User user, AddToCartRequest request) {
		BookEdition edition = editionRepository.findById(request.getEditionId())
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên bản sách."));
		validateAvailability(edition, request.getQuantity());

		List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());

		CartItem existing = cartItems.stream().filter(item -> item.getEdition().getId().equals(edition.getId()))
				.findFirst().orElse(null);

		if (existing != null) {
			int quantity = existing.getQuantity() + request.getQuantity();
			validateAvailability(edition, quantity);
			existing.setQuantity(quantity);
			cartItemRepository.save(existing);
		} else {
			CartItem newItem = CartItem.builder().user(user).edition(edition).quantity(request.getQuantity()).build();
			cartItemRepository.save(newItem);
			cartItems.add(newItem);
		}
		return orderMapper.toCartResponse(cartItemRepository.findByUserId(user.getId()));
	}

	@Transactional
	public CartResponse updateItem(User user, Long itemId, int quantity) {
		CartItem item = ownedItem(user, itemId);
		validateAvailability(item.getEdition(), quantity);
		item.setQuantity(quantity);
		cartItemRepository.save(item);
		return orderMapper.toCartResponse(cartItemRepository.findByUserId(user.getId()));
	}

	@Transactional
	public CartResponse removeItem(User user, Long itemId) {
		CartItem item = ownedItem(user, itemId);
		cartItemRepository.delete(item);
		return orderMapper.toCartResponse(cartItemRepository.findByUserId(user.getId()));
	}

	private CartItem ownedItem(User user, Long itemId) {
		List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
		return cartItems.stream().filter(item -> item.getId().equals(itemId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng."));
	}

	private void validateAvailability(BookEdition edition, int quantity) {
		if (!Boolean.TRUE.equals(edition.getIsActive())) {
			throw new BadRequestException("Phiên bản sách hiện không còn được bán.");
		}
		if (!"PHYSICAL".equals(edition.getFormat()) && quantity > 1) {
			throw new BadRequestException("Sách điện tử chỉ được mua một bản cho mỗi tài khoản.");
		}
		if ("PHYSICAL".equals(edition.getFormat()) && (edition.getStock() == null || edition.getStock() < quantity)) {
			throw new BadRequestException("Số lượng sách trong kho không đủ.");
		}
	}
}
