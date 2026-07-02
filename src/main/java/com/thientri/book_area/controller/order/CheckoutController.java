package com.thientri.book_area.controller.order;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.dto.request.order.CreateOrderRequest;
import com.thientri.book_area.dto.response.order.CheckoutResponse;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.payment.PaymentMethodRepository;
import com.thientri.book_area.repository.user.AddressRepository;
import com.thientri.book_area.service.order.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CheckoutController {
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderService orderService;

    @GetMapping("/checkout/options")
    public ResponseEntity<Map<String, Object>> options(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of(
                "addresses", addressRepository.findByUserId(user.getId()).stream().map(address -> Map.of(
                        "id", address.getId(),
                        "recipientName", address.getRecipientName(),
                        "recipientPhone", address.getRecipientPhone(),
                        "addressLine", address.getAddressLine(),
                        "provinceName", address.getProvinceName(),
                        "districtName", address.getDistrictName(),
                        "wardName", address.getWardName(),
                        "isDefault", Boolean.TRUE.equals(address.getIsDefault()))).toList(),
                "paymentMethods", paymentMethodRepository.findAll().stream()
                        .filter(method -> "VNPay".equalsIgnoreCase(method.getName()) || "COD".equalsIgnoreCase(method.getName()))
                        .toList()));
    }

    @PostMapping("/orders")
    public ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request, HttpServletRequest servletRequest) {
        String forwarded = servletRequest.getHeader("X-Forwarded-For");
        String clientIp = forwarded == null ? servletRequest.getRemoteAddr() : forwarded.split(",")[0].trim();
        return ResponseEntity.ok(orderService.checkout(user, request, clientIp));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> history(@AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.history(user, page, size));
    }
}
