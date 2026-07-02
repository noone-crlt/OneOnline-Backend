package com.thientri.book_area.repository.engagement;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.engagement.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Chỉ lấy các review đã được duyệt để hiển thị cho Frontend
    Page<Review> findByBookIdAndIsApprovedTrueOrderByCreatedAtDesc(Long bookId, Pageable pageable);
    
    // Kiểm tra user đã review sách này chưa
    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);

    @Query("select count(r) from Review r where r.comment is not null and trim(r.comment) <> ''")
    long countComments();

    @Query("select count(r) from Review r where r.comment is not null and trim(r.comment) <> '' "
            + "and r.createdAt >= :start and r.createdAt < :end")
    long countCommentsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
