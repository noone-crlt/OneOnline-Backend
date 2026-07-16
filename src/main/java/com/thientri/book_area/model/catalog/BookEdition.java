package com.thientri.book_area.model.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.thientri.book_area.model.audio.EditionAudioChapter;
import com.thientri.book_area.model.audio.Narrator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book_editions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookEdition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	// 'PHYSICAL', 'EBOOK_PDF', 'EBOOK_EPUB', 'AUDIOBOOK'
	@Column(name = "format", length = 50, nullable = false)
	private String format;

	@Column(name = "sku_code", length = 100, unique = true)
	private String skuCode;

	@Column(name = "original_price", precision = 18, scale = 0)
	private BigDecimal originalPrice;

	@Column(name = "sale_price", precision = 18, scale = 0, nullable = false)
	private BigDecimal salePrice;

	@Column(name = "stock")
	private Integer stock;

	@Column(name = "is_active")
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "cover_object_name", length = 500)
	private String coverObjectName;

	@Column(name = "file_object_name", length = 500)
	private String fileObjectName;

	@Column(name = "duration")
	private Integer duration;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	// Thêm vào bên trong class BookEdition.java
	@ManyToMany
	@JoinTable(name = "edition_narrators", joinColumns = @JoinColumn(name = "edition_id"), inverseJoinColumns = @JoinColumn(name = "narrator_id"))
	@Builder.Default
	private Set<Narrator> narrators = new HashSet<>();

	// THÊM MỚI: Liên kết 1 Phiên bản với nhiều Chương sách nói
	@OneToMany(mappedBy = "edition", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<EditionAudioChapter> audioChapters = new java.util.ArrayList<>();
}
