package com.thientri.book_area.controller.payment;

import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.payment.PaymentStatusResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thientri.book_area.dto.request.payment.WebhookRequest;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.service.order.OrderService;
import com.thientri.book_area.service.payment.SePayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
	private final OrderService orderService;
	private final SePayService sePayService;
	private final ObjectMapper objectMapper;

	@PostMapping("/sepay-webhook")
	public ResponseEntity<?> sePayWebhook(@RequestBody byte[] rawBody,
			@RequestHeader(value = "X-SePay-Signature", required = false) String signature,
			@RequestHeader(value = "X-SePay-Timestamp", required = false) String timestamp) {
		try {
			sePayService.verifySignature(rawBody, signature, timestamp);
		} catch (BadRequestException exception) {
			log.warn("SePay webhook có chữ ký không hợp lệ: {}", exception.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(exception.getMessage()));
		}

		try {
			WebhookRequest request = objectMapper.readValue(rawBody, WebhookRequest.class);
			orderService.handleSePayWebhook(request, new String(rawBody, StandardCharsets.UTF_8));
			return ResponseEntity.ok(Map.of("success", true));
		} catch (BadRequestException exception) {
			log.warn("SePay webhook không áp dụng giao dịch: {}", exception.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(Map.of("success", false, "message", exception.getMessage()));
		} catch (ResourceNotFoundException exception) {
			log.warn("SePay webhook không khớp đơn hàng: {}", exception.getMessage());
			return ResponseEntity.unprocessableEntity()
					.body(Map.of("success", false, "message", exception.getMessage()));
		} catch (Exception exception) {
			log.error("Không thể xử lý webhook SePay.", exception);
			return ResponseEntity.internalServerError()
					.body(ApiResponse.error("Không thể xử lý webhook SePay."));
		}
	}

	@GetMapping("/{orderCode}/status")
	public ResponseEntity<ApiResponse<PaymentStatusResponse>> paymentStatus(@PathVariable String orderCode,
			@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(ApiResponse.success(orderService.paymentStatus(user, orderCode)));
	}
}
