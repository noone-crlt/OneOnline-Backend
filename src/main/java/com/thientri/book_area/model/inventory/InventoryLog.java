package com.thientri.book_area.model.inventory;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.user.User;

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
@Table(name = "inventory_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// SỬA LỖI KIẾN TRÚC: Phải trỏ về Edition (Phiên bản sách), không phải sách gốc
	@ManyToOne
	@JoinColumn(name = "edition_id", nullable = false)
	private BookEdition edition;

	@Column(name = "change_amount", nullable = false)
	private Integer changeAmount;

	@Column(name = "reason", length = 255)
	private String reason;

	// THÊM MỚI: Dấu vết kiểm toán (Ai là người sửa kho?)
	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
