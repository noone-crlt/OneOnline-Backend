package com.thientri.book_area.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.admin.AdminUserResponse;
import com.thientri.book_area.service.admin.IAdminUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final IAdminUserService adminUserService;

	@GetMapping
	public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAdminUsers(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String role,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
		Page<AdminUserResponse> users = adminUserService.getAdminUsers(search, role, status, pageable);
		return ResponseEntity.ok(ApiResponse.success("Tải danh sách người dùng thành công.", users));
	}

	@PatchMapping("/{id}/ban")
	public ResponseEntity<ApiResponse<Void>> toggleBanUser(
			@PathVariable Long id,
			@RequestParam boolean isBanned) {

		adminUserService.toggleBanUser(id, isBanned);
		String message = isBanned ? "Đã khóa tài khoản người dùng." : "Đã gỡ khóa tài khoản người dùng.";
		return ResponseEntity.ok(ApiResponse.success(message, null));
	}
}
