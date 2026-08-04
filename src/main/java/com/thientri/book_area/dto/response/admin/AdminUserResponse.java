package com.thientri.book_area.dto.response.admin;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {
	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private List<String> roles;
	private String status;
	private String banReason;
	private LocalDateTime bannedAt;
	private LocalDateTime createdAt;
}
