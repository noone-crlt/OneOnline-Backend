package com.thientri.book_area.model.engagement;

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
// SỬA: Đảm bảo mỗi user chỉ có 1 dòng tiến độ duy nhất cho 1 phiên bản sách
@Table(name = "listen_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "edition_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListenProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "edition_id", nullable = false)
    private BookEdition edition;

    // Lưu số giây hoặc % đã nghe
    @Column(name = "progress")
    @Builder.Default
    private Integer progress = 0;
}