package com.thientri.book_area.repository.payment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.payment.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Tìm giao dịch bằng mã của ngân hàng/MoMo trả về
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Tìm chứng từ thanh toán của một đơn hàng cụ thể
    Optional<Payment> findByOrderId(Long orderId);
}