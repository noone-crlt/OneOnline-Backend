package com.thientri.book_area.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.thientri.book_area.dto.response.admin.AdminUserResponse;

public interface IAdminUserService {

	Page<AdminUserResponse> getAdminUsers(String search, String role, String status, Pageable pageable);

	void toggleBanUser(Long userId, boolean isBanned);
}
