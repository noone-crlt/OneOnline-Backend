package com.thientri.book_area.controller.payment;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.model.payment.Payment;
import com.thientri.book_area.service.order.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final OrderService orderService;

    @Value("${app.frontend.payment-result-url}")
    private String resultUrl;

    @GetMapping("/vnpay-return")
    public ResponseEntity<Void> vnPayReturn(@RequestParam Map<String, String> params) {
        String status = "failed";
        String orderCode = params.getOrDefault("vnp_TxnRef", "");
        try {
            Payment payment = orderService.handleVnPayReturn(params);
            status = payment.getStatus().name().toLowerCase();
        } catch (RuntimeException exception) {
            status = "invalid";
        }
        String query = "status=" + encode(status) + "&orderCode=" + encode(orderCode);
        return ResponseEntity.status(302).location(URI.create(resultUrl + "?" + query)).build();
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnPayIpn(@RequestParam Map<String, String> params) {
        try {
            orderService.handleVnPayReturn(params);
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.ok(Map.of("RspCode", "01", "Message", "Order not found"));
        } catch (BadRequestException exception) {
            String responseCode = exception.getMessage().contains("Chữ ký") ? "97" : "04";
            return ResponseEntity.ok(Map.of("RspCode", responseCode, "Message", exception.getMessage()));
        } catch (RuntimeException exception) {
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "Unknown error"));
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
