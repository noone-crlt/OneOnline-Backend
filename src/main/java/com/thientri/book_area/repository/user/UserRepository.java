package com.thientri.book_area.repository.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm người dùng bằng email (dùng cho đăng nhập)
    Optional<User> findByEmail(String email);
    
    // Kiểm tra xem email hoặc sđt đã tồn tại chưa (dùng cho đăng ký)
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}