package com.thientri.book_area.repository.user;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thientri.book_area.model.user.User;
import com.thientri.book_area.model.user.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmail(String email);
	boolean existsByPhone(String phone);

	long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

	@Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r " +
			"WHERE (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))) " +
			"AND (:role IS NULL OR :role = '' OR LOWER(r.name) = LOWER(:role)) " +
			"AND (:status IS NULL OR u.status = :status)")
	Page<User> findAdminUsers(@Param("search") String search, @Param("role") String role, @Param("status") UserStatus status, Pageable pageable);
}
