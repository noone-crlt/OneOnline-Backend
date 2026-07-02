package com.thientri.book_area.repository.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.user.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    
    // Tìm địa chỉ mặc định của người dùng khi họ tiến hành thanh toán
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}