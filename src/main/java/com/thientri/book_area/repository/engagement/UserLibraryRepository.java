package com.thientri.book_area.repository.engagement;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.engagement.UserLibrary;

@Repository
public interface UserLibraryRepository extends JpaRepository<UserLibrary, Long> {
    // Lấy tủ sách của người dùng
    Page<UserLibrary> findByUserIdOrderByAcquiredAtDesc(Long userId, Pageable pageable);
    
    // Kiểm tra khách đã sở hữu phiên bản sách này chưa (để chặn mua trùng)
    boolean existsByUserIdAndEditionId(Long userId, Long editionId);
}
