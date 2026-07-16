package com.thientri.book_area.model.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "expiry_date", updatable = false)
	private LocalDateTime expiryDate;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// SỬA: Bổ sung unique = true để chống lưu trùng token
	@Column(name = "refresh_token", length = 255, nullable = false, unique = true)
	private String refreshToken;

	// THÊM MỚI: Dấu vết thời gian tạo
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	// THÊM MỚI: Dùng Boolean chuẩn chỉnh thay vì String
	@Column(name = "revoked")
	@Builder.Default
	private Boolean revoked = false;
}
