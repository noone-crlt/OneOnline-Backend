package com.thientri.book_area.service.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private static final ZoneId VIETNAM_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MONTH_COUNT = 6;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final EditionAudioChapterRepository chapterRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        return AdminDashboardSummaryResponse.builder()
                .totalUsers(userRepository.count())
                .totalBooks(bookRepository.count())
                .totalCategories(categoryRepository.count())
                .totalAuthors(authorRepository.count())
                .totalChapters(chapterRepository.count())
                .totalComments(reviewRepository.countComments())
                .totalRevenue(paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS))
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminMonthlyStatsResponse> getChartStats(String granularity) {
        if ("daily".equalsIgnoreCase(granularity)) {
            return getDailyStats();
        }
        return getMonthlyStats();
    }

    private List<AdminMonthlyStatsResponse> getMonthlyStats() {
        YearMonth currentMonth = YearMonth.now(VIETNAM_TIME_ZONE);
        List<AdminMonthlyStatsResponse> result = new ArrayList<>(MONTH_COUNT);

        for (int offset = MONTH_COUNT - 1; offset >= 0; offset--) {
            YearMonth month = currentMonth.minusMonths(offset);
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDate firstDayOfNextMonth = month.plusMonths(1).atDay(1);
            LocalDateTime end = firstDayOfNextMonth.atStartOfDay();

            result.add(AdminMonthlyStatsResponse.builder()
                    .month(month.toString())
                    .newUsers(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
                    .newBooks(bookRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
                    .comments(reviewRepository.countCommentsBetween(start, end))
                    .revenue(paymentRepository.sumAmountByStatusBetween(PaymentStatus.SUCCESS, start, end))
                    .build());
        }
        return result;
    }

    private List<AdminMonthlyStatsResponse> getDailyStats() {
        LocalDate currentDate = LocalDate.now(VIETNAM_TIME_ZONE);
        int dayCount = 7;
        List<AdminMonthlyStatsResponse> result = new ArrayList<>(dayCount);

        for (int offset = dayCount - 1; offset >= 0; offset--) {
            LocalDate day = currentDate.minusDays(offset);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();

            result.add(AdminMonthlyStatsResponse.builder()
                    .month(day.toString())
                    .newUsers(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
                    .newBooks(bookRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
                    .comments(reviewRepository.countCommentsBetween(start, end))
                    .revenue(paymentRepository.sumAmountByStatusBetween(PaymentStatus.SUCCESS, start, end))
                    .build());
        }
        return result;
    }
}
