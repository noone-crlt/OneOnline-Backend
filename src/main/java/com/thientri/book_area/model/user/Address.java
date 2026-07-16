package com.thientri.book_area.model.user;

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
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "recipient_name", length = 255, nullable = false)
	private String recipientName;

	@Column(name = "recipient_phone", length = 20, nullable = false)
	private String recipientPhone;

	@Column(name = "address_line", columnDefinition = "NVARCHAR(MAX)", nullable = false)
	private String addressLine;

	@Column(name = "province_id", length = 50, nullable = false)
	private String provinceId;

	@Column(name = "district_id", length = 50, nullable = false)
	private String districtId;

	@Column(name = "ward_id", length = 50, nullable = false)
	private String wardId;

	@Column(name = "province_name", length = 100, nullable = false)
	private String provinceName;

	@Column(name = "district_name", length = 100, nullable = false)
	private String districtName;

	@Column(name = "ward_name", length = 100, nullable = false)
	private String wardName;

	@Column(name = "is_default")
	@Builder.Default
	private Boolean isDefault = false; // Sử dụng Boolean object thay vì bit thuần túy
}
