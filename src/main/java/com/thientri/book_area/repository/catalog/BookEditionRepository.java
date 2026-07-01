package com.thientri.book_area.repository.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.catalog.BookEdition;

@Repository
public interface BookEditionRepository extends JpaRepository<BookEdition, Long> {
    
    // Tìm phiên bản cụ thể bằng mã kho
    Optional<BookEdition> findBySkuCode(String skuCode);
}