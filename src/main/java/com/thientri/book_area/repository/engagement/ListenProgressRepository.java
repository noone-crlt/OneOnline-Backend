package com.thientri.book_area.repository.engagement;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thientri.book_area.model.engagement.ListenProgress;

@Repository
public interface ListenProgressRepository extends JpaRepository<ListenProgress, Long> {
    Optional<ListenProgress> findByUserIdAndEditionId(Long userId, Long editionId);
}