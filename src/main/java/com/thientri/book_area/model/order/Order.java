package com.thientri.book_area.model.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.thientri.book_area.model.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_code", unique = true, nullable = false, length = 50)
	private String orderCode;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "status", length = 50, nullable = false)
	@Builder.Default
	private String status = "PENDING";

	// ==========================================
	// SNAPSHOT ĐỊA CHỈ (Đóng băng lịch sử giao hàng)
	// ==========================================
	@Column(name = "recipient_name", length = 255)
	private String recipientName;

	@Column(name = "recipient_phone", length = 20)
	private String recipientPhone;

	@Column(name = "shipping_address_line", columnDefinition = "NVARCHAR(MAX)")
	private String shippingAddressLine;

	@Column(name = "shipping_province_name", length = 100)
	private String shippingProvinceName;

	@Column(name = "shipping_district_name", length = 100)
	private String shippingDistrictName;

	@Column(name = "shipping_ward_name", length = 100)
	private String shippingWardName;

	@Column(name = "tracking_code", length = 100)
	private String trackingCode;

	// ==========================================
	// BÁO CÁO KẾ TOÁN (Đóng băng dòng tiền)
	// ==========================================
	@Column(name = "sub_total", precision = 18, scale = 0, nullable = false)
	private BigDecimal subTotal;

	@Column(name = "shipping_fee", precision = 18, scale = 0, nullable = false)
	private BigDecimal shippingFee;

	@Column(name = "applied_coupon_code", length = 50)
	private String appliedCouponCode;

	@Column(name = "discount_amount", precision = 18, scale = 0)
	@Builder.Default
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@Column(name = "total_amount", precision = 18, scale = 0, nullable = false)
	private BigDecimal totalAmount;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<OrderItem> orderItems = new ArrayList<>();

	// Helper method đồng bộ 2 chiều an toàn
	public void addOrderItem(OrderItem item) {
		orderItems.add(item);
		item.setOrder(this);
	}

	public void removeOrderItem(OrderItem item) {
		orderItems.remove(item);
		item.setOrder(null);
	}
}
