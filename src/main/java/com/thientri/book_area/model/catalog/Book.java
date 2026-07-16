package com.thientri.book_area.model.catalog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
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
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", length = 255, nullable = false)
	private String title;

	// THÊM MỚI: Bắt buộc phải có slug (URL thân thiện) theo DB
	@Column(name = "slug", length = 255, nullable = false, unique = true)
	private String slug;

	@Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
	private String description;

	@ManyToOne
	@JoinColumn(name = "publisher_id")
	private Publisher publisher;

	@Column(name = "is_active")
	@Builder.Default
	private Boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	// Lưu ý: Dùng @ManyToMany tạm thời chấp nhận mất cột author_role.
	// Nếu muốn lưu author_role, phải tách thành Entity BookAuthor riêng
	// (@OneToMany).
	@ManyToMany
	@JoinTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
	@Builder.Default
	private Set<Category> categories = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
	@Builder.Default
	private Set<Author> authors = new HashSet<>();

	@Column(name = "images", columnDefinition = "NVARCHAR(MAX)")
	private String images;

	// THÊM MỚI: Liên kết 1 cuốn sách gốc với nhiều định dạng (Variant)
	@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<BookEdition> editions = new ArrayList<>();
}
