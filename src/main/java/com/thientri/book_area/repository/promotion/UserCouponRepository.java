package com.thientri.book_area.repository.promotion;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.promotion.UserCoupon;
import com.thientri.book_area.model.promotion.UserCouponId;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, UserCouponId> {
    // Kiểm tra xem User đã lưu/sử dụng mã này chưa
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
}