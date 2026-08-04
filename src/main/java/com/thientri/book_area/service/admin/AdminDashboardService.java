package com.thientri.book_area.service.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.response.admin.AdminDashboardSummaryResponse;
import com.thientri.book_area.dto.response.admin.AdminMonthlyStatsResponse;
import com.thientri.book_area.dto.response.admin.AdminTopBookResponse;
import com.thientri.book_area.model.catalog.Author;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.catalog.Category;
import com.thientri.book_area.model.payment.PaymentStatus;
import com.thientri.book_area.repository.audio.EditionAudioChapterRepository;
import com.thientri.book_area.repository.catalog.AuthorRepository;
import com.thientri.book_area.repository.catalog.BookRepository;
import com.thientri.book_area.repository.catalog.CategoryRepository;
import com.thientri.book_area.repository.engagement.ReviewRepository;
import com.thientri.book_area.repository.payment.PaymentRepository;
import com.thientri.book_area.repository.user.UserRepository;
import com.thientri.book_area.service.minio.MinioService;

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
	private final MinioService minioService;

	@Transactional(readOnly = true)
	public AdminDashboardSummaryResponse getSummary() {
		return AdminDashboardSummaryResponse.builder().totalUsers(userRepository.count())
				.totalBooks(bookRepository.count()).totalCategories(categoryRepository.count())
				.totalAuthors(authorRepository.count()).totalChapters(chapterRepository.count())
				.totalComments(reviewRepository.countComments())
				.totalRevenue(paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS)).build();
	}

	@Transactional(readOnly = true)
	public List<AdminMonthlyStatsResponse> getChartStats(String granularity) {
		if ("daily".equalsIgnoreCase(granularity)) {
			return getDailyStats();
		}
		return getMonthlyStats();
	}

	@Transactional(readOnly = true)
	public List<AdminTopBookResponse> getTopSellingBooks(int limit) {
		int targetLimit = Math.max(1, Math.min(limit, 50));
		Pageable pageable = PageRequest.of(0, targetLimit);
		List<Object[]> stats = bookRepository.findTopSellingBookStats(pageable);

		List<AdminTopBookResponse> result = new ArrayList<>();
		Set<Long> processedBookIds = new HashSet<>();

		for (Object[] row : stats) {
			if (row[0] == null) continue;
			Long bookId = ((Number) row[0]).longValue();
			Long totalSold = row[1] != null ? ((Number) row[1]).longValue() : 0L;
			BigDecimal totalRevenue = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

			Optional<Book> bookOpt = bookRepository.findById(bookId);
			if (bookOpt.isPresent()) {
				processedBookIds.add(bookId);
				result.add(buildTopBookResponse(bookOpt.get(), totalSold, totalRevenue));
			}
		}

		// Nếu kết quả chưa đủ limit, bổ sung các sách trong kho
		if (result.size() < targetLimit) {
			List<Book> allBooks = bookRepository.findAll(PageRequest.of(0, targetLimit)).getContent();
			for (Book book : allBooks) {
				if (result.size() >= targetLimit) break;
				if (!processedBookIds.contains(book.getId())) {
					processedBookIds.add(book.getId());
					result.add(buildTopBookResponse(book, 0L, BigDecimal.ZERO));
				}
			}
		}

		return result;
	}

	private AdminTopBookResponse buildTopBookResponse(Book book, Long totalSold, BigDecimal totalRevenue) {
		String authors = "";
		if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
			authors = book.getAuthors().stream()
					.map(Author::getName)
					.filter(name -> name != null && !name.isBlank())
					.collect(Collectors.joining(", "));
		}
		if (authors.isBlank()) {
			authors = "Chưa rõ tác giả";
		}

		String categories = "";
		if (book.getCategories() != null && !book.getCategories().isEmpty()) {
			categories = book.getCategories().stream()
					.map(Category::getName)
					.filter(name -> name != null && !name.isBlank())
					.collect(Collectors.joining(", "));
		}
		if (categories.isBlank()) {
			categories = "Đang cập nhật";
		}

		String coverUrl = resolveBookCoverUrl(book);

		return AdminTopBookResponse.builder()
				.bookId(book.getId())
				.title(book.getTitle())
				.slug(book.getSlug())
				.coverUrl(coverUrl)
				.authorName(authors)
				.categoryName(categories)
				.totalSold(totalSold)
				.totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
				.build();
	}

	private String resolveBookCoverUrl(Book book) {
		if (book.getEditions() != null && !book.getEditions().isEmpty()) {
			for (BookEdition ed : book.getEditions()) {
				if (ed.getCoverObjectName() != null && !ed.getCoverObjectName().isBlank()) {
					return minioService.getPresignedUrl(ed.getCoverObjectName());
				}
			}
		}
		if (book.getImages() != null && !book.getImages().isBlank()) {
			return minioService.getPresignedUrl(book.getImages());
		}
		return null;
	}

	private List<AdminMonthlyStatsResponse> getMonthlyStats() {
		YearMonth currentMonth = YearMonth.now(VIETNAM_TIME_ZONE);
		List<AdminMonthlyStatsResponse> result = new ArrayList<>(MONTH_COUNT);

		for (int offset = MONTH_COUNT - 1; offset >= 0; offset--) {
			YearMonth month = currentMonth.minusMonths(offset);
			LocalDateTime start = month.atDay(1).atStartOfDay();
			LocalDate firstDayOfNextMonth = month.plusMonths(1).atDay(1);
			LocalDateTime end = firstDayOfNextMonth.atStartOfDay();

			result.add(AdminMonthlyStatsResponse.builder().month(month.toString())
					.newUsers(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
					.newBooks(bookRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
					.comments(reviewRepository.countCommentsBetween(start, end))
					.revenue(paymentRepository.sumAmountByStatusBetween(PaymentStatus.SUCCESS, start, end)).build());
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

			result.add(AdminMonthlyStatsResponse.builder().month(day.toString())
					.newUsers(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
					.newBooks(bookRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end))
					.comments(reviewRepository.countCommentsBetween(start, end))
					.revenue(paymentRepository.sumAmountByStatusBetween(PaymentStatus.SUCCESS, start, end)).build());
		}
		return result;
	}
}
