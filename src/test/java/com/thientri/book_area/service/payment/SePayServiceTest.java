package com.thientri.book_area.service.payment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.model.order.Order;
import com.thientri.book_area.model.payment.Payment;
import com.thientri.book_area.model.payment.PaymentStatus;

class SePayServiceTest {
	private static final String SECRET = "test-secret";
	private final SePayService service = new SePayService("VCB", "0123456789", "NGUYEN VAN A", SECRET, 15);

	@Test
	void createsQrWithExactAmountAndOrderCode() {
		Order order = Order.builder().orderCode("BA123ABC").build();
		Payment payment = Payment.builder().order(order).amount(new BigDecimal("125000")).status(PaymentStatus.PENDING)
				.build();

		var response = service.checkoutResponse(payment);

		assertEquals("https://vietqr.app/img?acc=0123456789&bank=VCB&amount=125000&des=SEVQR+BA123ABC",
				response.getPaymentUrl());
		assertEquals("SEVQR BA123ABC", response.getTransferContent());
	}

	@Test
	void verifiesValidHmacAndRejectsInvalidSignature() throws Exception {
		byte[] body = "{\"id\":123}".getBytes(StandardCharsets.UTF_8);
		String timestamp = String.valueOf(Instant.now().getEpochSecond());
		String signature = sign(timestamp, body);

		assertDoesNotThrow(() -> service.verifySignature(body, signature, timestamp));
		assertThrows(BadRequestException.class, () -> service.verifySignature(body, "sha256=invalid", timestamp));
	}

	private String sign(String timestamp, byte[] body) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
		return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));
	}
}
