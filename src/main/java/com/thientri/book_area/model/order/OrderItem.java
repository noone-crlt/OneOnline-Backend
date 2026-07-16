package com.thientri.book_area.model.order;

import java.math.BigDecimal;

import com.thientri.book_area.model.catalog.BookEdition;

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
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	// SỬA: Trỏ về Phiên bản sách (Edition), không trỏ về sách gốc
	@ManyToOne
	@JoinColumn(name = "edition_id", nullable = false)
	private BookEdition edition;

	@Column(name = "quantity", nullable = false)
	@Builder.Default
	private Integer quantity = 1;

	// THÊM: Giá gốc để biết khách đã được giảm bao nhiêu
	@Column(name = "original_price", precision = 18, scale = 0)
	private BigDecimal originalPrice;

	// Giá chốt mua cuối cùng
	@Column(name = "price", precision = 18, scale = 0, nullable = false)
	private BigDecimal price;
}
