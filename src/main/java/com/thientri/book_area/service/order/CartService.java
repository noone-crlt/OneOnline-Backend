package com.thientri.book_area.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.request.order.AddToCartRequest;
import com.thientri.book_area.dto.response.order.CartResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.mapper.OrderMapper;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.order.Cart;
import com.thientri.book_area.model.order.CartItem;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.catalog.BookEditionRepository;
import com.thientri.book_area.repository.order.CartRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final BookEditionRepository editionRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        return orderMapper.toCartResponse(findCart(user));
    }

    @Transactional
    public CartResponse addItem(User user, AddToCartRequest request) {
        Cart cart = findCart(user);
        BookEdition edition = editionRepository.findById(request.getEditionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên bản sách."));
        validateAvailability(edition, request.getQuantity());

        CartItem existing = cart.getCartItems().stream()
                .filter(item -> item.getEdition().getId().equals(edition.getId()))
                .findFirst().orElse(null);
        if (existing != null) {
            int quantity = existing.getQuantity() + request.getQuantity();
            validateAvailability(edition, quantity);
            existing.setQuantity(quantity);
        } else {
            cart.addCartItem(CartItem.builder().edition(edition).quantity(request.getQuantity()).build());
        }
        return orderMapper.toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(User user, Long itemId, int quantity) {
        Cart cart = findCart(user);
        CartItem item = ownedItem(cart, itemId);
        validateAvailability(item.getEdition(), quantity);
        item.setQuantity(quantity);
        return orderMapper.toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(User user, Long itemId) {
        Cart cart = findCart(user);
        cart.removeCartItem(ownedItem(cart, itemId));
        return orderMapper.toCartResponse(cartRepository.save(cart));
    }

    private Cart findCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    private CartItem ownedItem(Cart cart, Long itemId) {
        return cart.getCartItems().stream().filter(item -> item.getId().equals(itemId)).findFirst()
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
