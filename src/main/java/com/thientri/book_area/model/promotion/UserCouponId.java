package com.thientri.book_area.model.promotion;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponId implements Serializable {
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "coupon_id")
	private Long couponId;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		UserCouponId that = (UserCouponId) o;
		return Objects.equals(userId, that.userId) && Objects.equals(couponId, that.couponId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, couponId);
	}
}
