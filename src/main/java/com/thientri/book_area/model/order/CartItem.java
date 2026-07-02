package com.thientri.book_area.model.order;

import com.thientri.book_area.model.catalog.BookEdition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // SỬA LỖI KIẾN TRÚC: Trỏ về Edition, không trỏ về Book
    @ManyToOne
    @JoinColumn(name = "edition_id", nullable = false)
    private BookEdition edition;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}