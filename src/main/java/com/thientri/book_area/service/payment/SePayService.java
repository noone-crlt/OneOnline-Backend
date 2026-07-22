package com.thientri.book_area.service.payment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thientri.book_area.dto.response.order.CheckoutResponse;
import com.thientri.book_area.dto.response.payment.PaymentStatusResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.model.payment.Payment;

@Service
public class SePayService {
	private static final String VIETINBANK_PAYMENT_PREFIX = "SEVQR ";

	private final String bankCode;
	private final String accountNumber;
	private final String accountName;
	private final String webhookSecret;
	private final long timeoutMinutes;

	public SePayService(@Value("${sepay.bank-code:MB}") String bankCode,
			@Value("${sepay.account-number:123456789}") String accountNumber,
			@Value("${sepay.account-name:TEST ACCOUNT}") String accountName,
			@Value("${sepay.webhook-secret:}") String webhookSecret,
			@Value("${sepay.payment-timeout-minutes:60}") long timeoutMinutes) {
		this.bankCode = bankCode.trim();
		this.accountNumber = accountNumber.trim();
		this.accountName = accountName.trim();
		this.webhookSecret = webhookSecret.trim();
		this.timeoutMinutes = timeoutMinutes;
	}

	public CheckoutResponse checkoutResponse(Payment payment) {
		return CheckoutResponse.builder().orderCode(payment.getOrder().getOrderCode())
				.paymentStatus(payment.getStatus().name()).paymentUrl(qrUrl(payment)).bankCode(bankCode)
				.accountNumber(accountNumber).accountName(accountName).amount(payment.getAmount())
				.transferContent(paymentContent(payment)).expiresAt(expiresAt(payment)).build();
	}

	public PaymentStatusResponse statusResponse(Payment payment) {
		boolean sepay = "SEPAY".equalsIgnoreCase(payment.getPaymentMethod());
		return PaymentStatusResponse.builder().orderCode(payment.getOrder().getOrderCode())
				.status(payment.getStatus().name()).paymentMethod(payment.getPaymentMethod())
				.paymentUrl(sepay ? qrUrl(payment) : null).bankCode(sepay ? bankCode : null)
				.accountNumber(sepay ? accountNumber : null).accountName(sepay ? accountName : null)
				.amount(payment.getAmount()).transferContent(sepay ? paymentContent(payment) : null)
				.expiresAt(sepay ? expiresAt(payment) : null).build();
	}

	public void verifySignature(byte[] rawBody, String signature, String timestampValue) {
		try {
			long timestamp = Long.parseLong(timestampValue);
			if (Math.abs(Instant.now().getEpochSecond() - timestamp) > 300) {
				throw new BadRequestException("Webhook SePay đã hết hiệu lực.");
			}
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
			String expected = "sha256=" + HexFormat.of().formatHex(mac.doFinal(rawBody));
			if (signature == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
					signature.getBytes(StandardCharsets.US_ASCII))) {
				throw new BadRequestException("Chữ ký webhook SePay không hợp lệ.");
			}
		} catch (NumberFormatException exception) {
			throw new BadRequestException("Timestamp webhook SePay không hợp lệ.");
		} catch (BadRequestException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IllegalStateException("Không thể xác minh webhook SePay.", exception);
		}
	}

	public boolean isExpectedAccount(String value) {
		return accountNumber.equals(value);
	}

	private String qrUrl(Payment payment) {
		return "https://vietqr.app/img?acc=" + encode(accountNumber) + "&bank=" + encode(bankCode) + "&amount="
				+ payment.getAmount().setScale(0).toPlainString() + "&des=" + encode(paymentContent(payment));
	}

	private String paymentContent(Payment payment) {
		return VIETINBANK_PAYMENT_PREFIX + payment.getOrder().getOrderCode();
	}

	private OffsetDateTime expiresAt(Payment payment) {
		LocalDateTime createdAt = payment.getOrder().getCreatedAt();
		return (createdAt == null ? LocalDateTime.now(ZoneOffset.UTC) : createdAt).plusMinutes(timeoutMinutes)
				.atOffset(ZoneOffset.UTC);
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
