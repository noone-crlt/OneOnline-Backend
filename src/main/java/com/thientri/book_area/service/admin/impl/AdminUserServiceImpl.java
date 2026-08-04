package com.thientri.book_area.service.admin.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.response.admin.AdminUserResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.model.user.Role;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.model.user.UserStatus;
import com.thientri.book_area.repository.user.UserRepository;
import com.thientri.book_area.service.admin.IAdminUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements IAdminUserService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public Page<AdminUserResponse> getAdminUsers(String search, String role, String status, Pageable pageable) {
		UserStatus userStatus = null;
		if (status != null && !status.isBlank()) {
			try {
				userStatus = UserStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException ignored) {
			}
		}

		String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
		String normalizedRole = (role != null && !role.isBlank()) ? role.trim() : null;

		return userRepository.findAdminUsers(normalizedSearch, normalizedRole, userStatus, pageable)
				.map(this::toAdminUserResponse);
	}

	@Override
	@Transactional
	public void toggleBanUser(Long userId, boolean isBanned) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

		boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
				.anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()) || "ROLE_ADMIN".equalsIgnoreCase(r.getName()));

		if (isAdmin && isBanned) {
			throw new BadRequestException("Không thể khóa tài khoản Quản trị viên.");
		}

		if (isBanned) {
			user.setStatus(UserStatus.BANNED);
		} else {
			user.setStatus(UserStatus.ACTIVE);
		}

		userRepository.save(user);
	}

	private AdminUserResponse toAdminUserResponse(User user) {
		List<String> roleNames = user.getRoles() == null ? List.of()
				: user.getRoles().stream().map(Role::getName).toList();

		return AdminUserResponse.builder()
				.id(user.getId())
				.fullName(user.getFullName())
				.email(user.getEmail())
				.phone(user.getPhone())
				.roles(roleNames)
				.status(user.getStatus() == null ? "ACTIVE" : user.getStatus().name())
				.createdAt(user.getCreatedAt())
				.build();
	}
}
