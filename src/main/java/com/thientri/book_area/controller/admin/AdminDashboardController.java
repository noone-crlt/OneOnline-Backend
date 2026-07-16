package com.thientri.book_area.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.dto.response.admin.AdminDashboardSummaryResponse;
import com.thientri.book_area.dto.response.admin.AdminMonthlyStatsResponse;
import com.thientri.book_area.service.admin.AdminDashboardService;

import lombok.RequiredArgsConstructor;

import com.thientri.book_area.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
	private final AdminDashboardService dashboardService;

	@GetMapping("/summary")
	public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getSummary() {
		return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
	}

	@GetMapping("/monthly-stats")
	public ResponseEntity<ApiResponse<List<AdminMonthlyStatsResponse>>> getMonthlyStats(
			@RequestParam(defaultValue = "monthly") String granularity) {
		return ResponseEntity.ok(ApiResponse.success(dashboardService.getChartStats(granularity)));
	}
}
