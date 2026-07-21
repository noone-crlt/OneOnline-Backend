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
		if (clientId.isBlank()) {
			throw new BadRequestException("Đăng nhập Google chưa được cấu hình trên máy chủ.");
		}

		try {
			GoogleIdToken idToken = verifier.verify(credential);
			if (idToken == null) {
				throw new BadRequestException("Thông tin đăng nhập Google không hợp lệ hoặc đã hết hạn.");
			}

			GoogleIdToken.Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			if (!Boolean.TRUE.equals(payload.getEmailVerified()) || email == null || email.isBlank()
					|| payload.getSubject() == null || payload.getSubject().isBlank()) {
				throw new BadRequestException("Tài khoản Google chưa xác minh email.");
			}

			String name = payload.get("name") instanceof String value ? value.trim() : "";
			return new GoogleUserInfo(email.trim().toLowerCase(Locale.ROOT), name);
		} catch (GeneralSecurityException exception) {
			throw new BadRequestException("Thông tin đăng nhập Google không hợp lệ.");
		} catch (IOException exception) {
			throw new IllegalStateException("Không thể kết nối đến Google để xác minh đăng nhập.", exception);
		}
	}

	public record GoogleUserInfo(String email, String fullName) {
	}
}
