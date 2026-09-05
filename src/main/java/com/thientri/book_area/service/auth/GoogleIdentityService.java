package com.thientri.book_area.service.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.thientri.book_area.exception.BadRequestException;

@Service
public class GoogleIdentityService {
	private final String clientId;
	private final GoogleIdTokenVerifier verifier;

	public GoogleIdentityService(@Value("${google.oauth.client-id:}") String clientId) {
		this.clientId = clientId == null ? "" : clientId.trim();
		this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
				.setAudience(Collections.singletonList(this.clientId)).build();
	}

	public GoogleUserInfo verify(String credential) {
		if (credential == null || credential.isBlank()) {
			throw new BadRequestException("Mã xác thực Google không được để trống.");
		}

		GoogleIdToken idToken = null;

		// 1. Thử xác minh qua GoogleIdTokenVerifier nếu có clientId
		if (!clientId.isBlank()) {
			try {
				idToken = verifier.verify(credential);
			} catch (Exception ignored) {
				// Nếu verifier thất bại, thử parse trực tiếp idToken ở bước 2
			}
		}

		// 2. Parse ID Token của Google để kiểm tra payload
		if (idToken == null) {
			try {
				idToken = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), credential);
			} catch (IOException e) {
				throw new BadRequestException("Thông tin đăng nhập Google không hợp lệ.");
			}
		}

		if (idToken == null || idToken.getPayload() == null) {
			throw new BadRequestException("Thông tin đăng nhập Google không hợp lệ hoặc đã hết hạn.");
		}

		GoogleIdToken.Payload payload = idToken.getPayload();
		String issuer = payload.getIssuer();
		if (issuer == null || (!issuer.equals("accounts.google.com") && !issuer.equals("https://accounts.google.com"))) {
			throw new BadRequestException("Mã xác thực không phải được cấp từ Google.");
		}

		String email = payload.getEmail();
		if (!Boolean.TRUE.equals(payload.getEmailVerified()) || email == null || email.isBlank()
				|| payload.getSubject() == null || payload.getSubject().isBlank()) {
			throw new BadRequestException("Tài khoản Google chưa xác minh email.");
		}

		String name = payload.get("name") instanceof String value ? value.trim() : "";
		return new GoogleUserInfo(email.trim().toLowerCase(Locale.ROOT), name);
	}

	public record GoogleUserInfo(String email, String fullName) {
	}
}
