package com.thientri.book_area.model.engagement;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// SỬA: Thêm UniqueConstraint chặn việc 1 user đánh giá 1 cuốn sách nhiều lần
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
// SỬA: Bổ sung Lombok
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // Khoảng giá trị biên 1-5 được Database bảo vệ, Java cần đảm bảo không Null
    @Column(name = "rating", nullable = false)
    private Integer rating;

    // SỬA: Đổi TEXT thành NVARCHAR(MAX)
    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    // THÊM MỚI: Cơ chế kiểm duyệt bình luận (Admin duyệt mới được hiện)
    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}