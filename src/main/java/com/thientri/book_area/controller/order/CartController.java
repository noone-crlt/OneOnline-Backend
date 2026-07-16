package com.thientri.book_area.controller.order;

import com.thientri.book_area.dto.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.dto.request.order.AddToCartRequest;
import com.thientri.book_area.dto.request.order.UpdateCartItemRequest;
import com.thientri.book_area.dto.response.order.CartResponse;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.service.order.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;

	@GetMapping
	public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(ApiResponse.success(cartService.getCart(user)));
	}

	@PostMapping("/items")
	public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal User user,
			@Valid @RequestBody AddToCartRequest request) {
		return ResponseEntity.ok(ApiResponse.success(cartService.addItem(user, request)));
	}

	@PutMapping("/items/{itemId}")
	public ResponseEntity<ApiResponse<CartResponse>> updateItem(@AuthenticationPrincipal User user,
			@PathVariable Long itemId, @Valid @RequestBody UpdateCartItemRequest request) {
		return ResponseEntity.ok(ApiResponse.success(cartService.updateItem(user, itemId, request.getQuantity())));
	}

	@DeleteMapping("/items/{itemId}")
	public ResponseEntity<ApiResponse<CartResponse>> removeItem(@AuthenticationPrincipal User user,
			@PathVariable Long itemId) {
		return ResponseEntity.ok(ApiResponse.success(cartService.removeItem(user, itemId)));
	}
}
