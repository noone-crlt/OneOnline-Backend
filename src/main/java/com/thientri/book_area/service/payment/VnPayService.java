package com.thientri.book_area.service.payment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.model.order.Order;

@Service
public class VnPayService {
    private static final Logger log = LoggerFactory.getLogger(VnPayService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String SANDBOX_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    private final String payUrl;
    private final String tmnCode;
    private final String hashSecret;
    private final String returnUrl;

    public VnPayService(
            @Value("${vnpay.pay-url}") String payUrl,
            @Value("${vnpay.tmn-code}") String tmnCode,
            @Value("${vnpay.hash-secret}") String hashSecret,
            @Value("${vnpay.return-url}") String returnUrl) {
        this.payUrl = trim(payUrl);
        this.tmnCode = trim(tmnCode);
        this.hashSecret = trim(hashSecret);
        this.returnUrl = trim(returnUrl);
    }

    public String createPaymentUrl(Order order, String clientIp) {
        requireConfiguration();
        LocalDateTime now = LocalDateTime.now(VIETNAM_TIME_ZONE);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100))
                .toBigIntegerExact().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getOrderCode());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", normalizeClientIp(clientIp));
        params.put("vnp_CreateDate", now.format(DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMAT));

        String hashData = canonicalize(params);
        String secureHash = hmacSha512(hashSecret, hashData);
        log.debug("VNPay payment params before signing: {}", params);
        log.debug("VNPay hashData: {}", hashData);
        log.debug("VNPay generated secureHash: {}", secureHash);
        log.debug("VNPay configuration: tmnCode={}, hashSecretLength={}, returnUrl={}",
                mask(tmnCode), hashSecret.length(), returnUrl);
        return payUrl + "?" + hashData + "&vnp_SecureHash=" + secureHash;
    }

    public boolean verify(Map<String, String> input) {
        if (hashSecret.isBlank() || input == null) return false;
        String receivedHash = input.get("vnp_SecureHash");
        Map<String, String> params = input.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vnp_"))
                .filter(entry -> !"vnp_SecureHash".equals(entry.getKey()))
                .filter(entry -> !"vnp_SecureHashType".equals(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (first, ignored) -> first, TreeMap::new));
        String hashData = canonicalize(params);
        String generatedHash = hmacSha512(hashSecret, hashData);
        log.debug("VNPay callback params before verification: {}", params);
        log.debug("VNPay callback hashData: {}", hashData);
        log.debug("VNPay callback generated secureHash: {}", generatedHash);
        return receivedHash != null && constantTimeEquals(receivedHash, generatedHash);
    }

    String canonicalize(Map<String, String> params) {
        return new TreeMap<>(params).entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vnp_"))
                .filter(entry -> !"vnp_SecureHash".equals(entry.getKey()))
                .filter(entry -> !"vnp_SecureHashType".equals(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    String sign(String hashData) {
        return hmacSha512(hashSecret, hashData);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể ký yêu cầu VNPay.", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.toLowerCase().getBytes(StandardCharsets.US_ASCII),
                right.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    private void requireConfiguration() {
        if (tmnCode.isBlank() || hashSecret.isBlank() || returnUrl.isBlank()) {
            throw new BadRequestException("VNPay chưa được cấu hình đầy đủ.");
        }
        if (!SANDBOX_PAY_URL.equals(payUrl)) {
            throw new BadRequestException("Địa chỉ cổng thanh toán VNPay sandbox không hợp lệ.");
        }
    }

    private static String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) return "127.0.0.1";
        String normalized = clientIp.trim();
        return "0:0:0:0:0:0:0:1".equals(normalized) || "::1".equals(normalized)
                ? "127.0.0.1" : normalized;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String mask(String value) {
        if (value.length() <= 4) return "****";
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
