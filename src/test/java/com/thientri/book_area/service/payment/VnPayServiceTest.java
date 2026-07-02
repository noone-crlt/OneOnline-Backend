package com.thientri.book_area.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class VnPayServiceTest {
    private static final String PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String RETURN_URL = "http://localhost:8080/api/payments/vnpay-return";
    private static final String SECRET = "test-secret";

    private final VnPayService service = new VnPayService(PAY_URL, " TESTCODE ", " " + SECRET + " ",
            " " + RETURN_URL + " ");

    @Test
    void canonicalizeSortsVnpParamsAndUsesFormUrlEncoding() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("ignored", "value");
        params.put("vnp_ReturnUrl", RETURN_URL);
        params.put("vnp_OrderInfo", "Thanh toan don hang BA123");
        params.put("vnp_Amount", "1000000");

        assertEquals(
                "vnp_Amount=1000000&vnp_OrderInfo=Thanh+toan+don+hang+BA123"
                        + "&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fpayments%2Fvnpay-return",
                service.canonicalize(params));
    }

    @Test
    void signMatchesKnownHmacSha512Vector() {
        String hashData = "vnp_Amount=1000000&vnp_OrderInfo=Thanh+toan+don+hang+BA123"
                + "&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fpayments%2Fvnpay-return";

        assertEquals(
                "98750f996b0f1d1e4ee1a99c72534a021a04ee37f70f827f59482fbe30b7e8ed"
                        + "a42934edcb502f8e56dcae86c4e483ab430cb4612f8870af89423a5763bb40b5",
                service.sign(hashData));
    }

    @Test
    void verifyIgnoresNonVnpAndSecureHashTypeParams() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "1000000");
        params.put("vnp_OrderInfo", "Thanh toan don hang BA123");
        params.put("vnp_SecureHashType", "HmacSHA512");
        params.put("untrusted", "must-not-be-signed");
        String secureHash = service.sign(service.canonicalize(params));
        params.put("vnp_SecureHash", secureHash);

        assertTrue(service.verify(params));
        params.put("vnp_Amount", "2000000");
        assertFalse(service.verify(params));
    }
}
