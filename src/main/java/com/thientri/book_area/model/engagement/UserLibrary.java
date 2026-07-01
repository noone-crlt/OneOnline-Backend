package com.thientri.book_area.model.engagement;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.thientri.book_area.model.catalog.BookEdition;
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
// SỬA: Đảm bảo 1 user không thể sở hữu 2 lần cùng 1 phiên bản sách
@Table(name = "user_library", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "edition_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Trỏ về Edition thay vì Book gốc
    @ManyToOne
    @JoinColumn(name = "edition_id", nullable = false)
    private BookEdition edition;

    @CreationTimestamp
    @Column(name = "acquired_at", updatable = false)
    private LocalDateTime acquiredAt;
}