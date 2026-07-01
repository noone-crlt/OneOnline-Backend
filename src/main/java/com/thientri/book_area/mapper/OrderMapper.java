package com.thientri.book_area.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.thientri.book_area.dto.response.order.CartItemResponse;
import com.thientri.book_area.dto.response.order.CartResponse;
import com.thientri.book_area.dto.response.order.OrderResponse;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.order.Cart;
import com.thientri.book_area.model.order.CartItem;
import com.thientri.book_area.model.order.Order;
import com.thientri.book_area.model.order.OrderItem;

@Component
public class OrderMapper {

    // ==========================================
    // 1. MAPPER: Cart -> CartResponse
    // ==========================================
    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) return null;

        List<CartItemResponse> itemResponses = cart.getCartItems() == null ? 
            Collections.emptyList() : 
            cart.getCartItems().stream().map(this::toCartItemResponse).collect(Collectors.toList());

        // Backend tự tính tổng tiền giỏ hàng, tuyệt đối không phụ thuộc Client
        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .cartTotalAmount(totalAmount)
                .build();
    }

    public CartItemResponse toCartItemResponse(CartItem item) {
        if (item == null || item.getEdition() == null) return null;
        BookEdition edition = item.getEdition();

        BigDecimal price = edition.getSalePrice() != null ? edition.getSalePrice() : BigDecimal.ZERO;
        int qty = item.getQuantity() != null ? item.getQuantity() : 0;
        
        // Tính thành tiền cho dòng này
        BigDecimal subTotal = price.multiply(BigDecimal.valueOf(qty));

        return CartItemResponse.builder()
                .id(item.getId())
                .editionId(edition.getId())
                // Trích xuất an toàn từ Book gốc
                .bookTitle(edition.getBook() != null ? edition.getBook().getTitle() : "Sách không xác định")
                .format(edition.getFormat())
                .coverImageUrl(edition.getCoverObjectName())
                .salePrice(price)
                .quantity(qty)
                .subTotal(subTotal)
                .build();
    }

    // ==========================================
    // 2. MAPPER: Order -> OrderResponse
    // ==========================================
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        // Gộp địa chỉ thành 1 chuỗi duy nhất cho Frontend dễ hiển thị
        String fullAddress = String.format("%s, %s, %s, %s", 
            order.getShippingAddressLine(), 
            order.getShippingWardName(), 
            order.getShippingDistrictName(), 
            order.getShippingProvinceName());

        List<CartItemResponse> mappedItems = order.getOrderItems() == null ? 
            Collections.emptyList() : 
            order.getOrderItems().stream().map(this::toOrderItemResponse).collect(Collectors.toList());

        return OrderResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getOrderStatus() != null ? order.getOrderStatus().getName() : "UNKNOWN")
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .fullShippingAddress(fullAddress)
                .trackingCode(order.getTrackingCode())
                .subTotal(order.getSubTotal())
                .shippingFee(order.getShippingFee())
                .appliedCouponCode(order.getAppliedCouponCode())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .orderItems(mappedItems)
                .build();
    }

    // Tái sử dụng Cấu trúc CartItemResponse cho OrderItem để Frontend tái sử dụng UI Component
    private CartItemResponse toOrderItemResponse(OrderItem item) {
        if (item == null || item.getEdition() == null) return null;
        BookEdition edition = item.getEdition();
        
        BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
        int qty = item.getQuantity() != null ? item.getQuantity() : 0;

        return CartItemResponse.builder()
                .id(item.getId())
                .editionId(edition.getId())
                .bookTitle(edition.getBook() != null ? edition.getBook().getTitle() : "Sách không xác định")
                .format(edition.getFormat())
                .coverImageUrl(edition.getCoverObjectName())
                .salePrice(price) // Sử dụng giá chốt lúc mua trong OrderItem
                .quantity(qty)
                .subTotal(price.multiply(BigDecimal.valueOf(qty)))
                .build();
    }
}