package com.thientri.book_area.repository.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.user.RefreshToken;
import com.thientri.book_area.model.user.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    // Tìm token để đối chiếu khi người dùng xin cấp lại Access Token
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    
    // Xóa/Thu hồi toàn bộ token của một người dùng khi họ đổi mật khẩu hoặc bị ban
    void deleteByUser(User user);
}