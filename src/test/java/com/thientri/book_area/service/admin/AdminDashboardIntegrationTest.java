package com.thientri.book_area.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional(readOnly = true)
class AdminDashboardIntegrationTest {
	@Autowired
	private AdminDashboardService dashboardService;

	@Test
	void dashboardQueriesExecuteAgainstConfiguredDatabase() {
		assertNotNull(dashboardService.getSummary());
		assertEquals(6, dashboardService.getChartStats("monthly").size());
	}
}
