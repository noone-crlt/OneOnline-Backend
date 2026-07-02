package com.thientri.book_area.repository.payment;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.payment.Payment;
import com.thientri.book_area.model.payment.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Tìm giao dịch bằng mã của ngân hàng/MoMo trả về
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Tìm chứng từ thanh toán của một đơn hàng cụ thể
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderOrderCode(String orderCode);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status and p.paidAt >= :start and p.paidAt < :end")
    BigDecimal sumAmountByStatusBetween(
            @Param("status") PaymentStatus status,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);
}
