package com.thientri.book_area.repository.order;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	// Tìm đơn hàng theo mã giao dịch
	Optional<Order> findByOrderCode(String orderCode);

	// Lấy lịch sử mua hàng của một người dùng (hỗ trợ phân trang)
	Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
