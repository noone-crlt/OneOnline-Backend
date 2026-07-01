package com.thientri.book_area.repository.promotion;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.promotion.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // Tìm mã giảm giá do người dùng nhập vào
    Optional<Coupon> findByCode(String code);
}