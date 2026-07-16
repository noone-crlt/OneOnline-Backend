package com.thientri.book_area.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thientri.book_area.dto.response.admin.AdminDashboardSummaryResponse;
import com.thientri.book_area.dto.response.admin.AdminMonthlyStatsResponse;
import com.thientri.book_area.model.payment.PaymentStatus;
import com.thientri.book_area.repository.audio.EditionAudioChapterRepository;
import com.thientri.book_area.repository.catalog.AuthorRepository;
import com.thientri.book_area.repository.catalog.BookRepository;
import com.thientri.book_area.repository.catalog.CategoryRepository;
import com.thientri.book_area.repository.engagement.ReviewRepository;
import com.thientri.book_area.repository.payment.PaymentRepository;
import com.thientri.book_area.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private BookRepository bookRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private AuthorRepository authorRepository;
	@Mock
	private EditionAudioChapterRepository chapterRepository;
	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private PaymentRepository paymentRepository;
	@InjectMocks
	private AdminDashboardService dashboardService;

	@Test
	void summaryUsesOnlyRealMetricsAndSuccessfulRevenue() {
		when(userRepository.count()).thenReturn(12L);
		when(bookRepository.count()).thenReturn(8L);
		when(categoryRepository.count()).thenReturn(4L);
		when(authorRepository.count()).thenReturn(6L);
		when(chapterRepository.count()).thenReturn(15L);
		when(reviewRepository.countComments()).thenReturn(5L);
		when(paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS)).thenReturn(new BigDecimal("450000"));

		AdminDashboardSummaryResponse result = dashboardService.getSummary();

		assertEquals(12L, result.getTotalUsers());
		assertEquals(8L, result.getTotalBooks());
		assertEquals(4L, result.getTotalCategories());
		assertEquals(6L, result.getTotalAuthors());
		assertEquals(15L, result.getTotalChapters());
		assertEquals(5L, result.getTotalComments());
		assertEquals(new BigDecimal("450000"), result.getTotalRevenue());
		verify(paymentRepository).sumAmountByStatus(PaymentStatus.SUCCESS);
	}

	@Test
	void monthlyStatsReturnsSixChronologicalMonthsIncludingEmptyMonths() {
		when(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
				any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
		when(bookRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
				any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
		when(reviewRepository.countCommentsBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
		when(paymentRepository.sumAmountByStatusBetween(any(PaymentStatus.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(BigDecimal.ZERO);

		List<AdminMonthlyStatsResponse> result = dashboardService.getChartStats("monthly");
		YearMonth current = YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh"));

		assertEquals(6, result.size());
		assertEquals(current.minusMonths(5).toString(), result.get(0).getMonth());
		assertEquals(current.toString(), result.get(5).getMonth());
		result.forEach(item -> {
			assertEquals(0L, item.getNewUsers());
			assertEquals(0L, item.getNewBooks());
			assertEquals(0L, item.getComments());
			assertEquals(BigDecimal.ZERO, item.getRevenue());
		});
	}
}
