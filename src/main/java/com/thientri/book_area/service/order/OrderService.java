package com.thientri.book_area.service.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.request.order.CreateOrderRequest;
import com.thientri.book_area.dto.request.payment.WebhookRequest;
import com.thientri.book_area.dto.response.order.CheckoutResponse;
import com.thientri.book_area.dto.response.order.OrderResponse;
import com.thientri.book_area.dto.response.payment.PaymentStatusResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ConflictException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.mapper.OrderMapper;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.engagement.UserLibrary;
import com.thientri.book_area.model.order.CartItem;
import com.thientri.book_area.model.order.Order;
import com.thientri.book_area.model.order.OrderItem;
import com.thientri.book_area.model.payment.Payment;
import com.thientri.book_area.model.payment.PaymentStatus;
import com.thientri.book_area.model.promotion.Coupon;
import com.thientri.book_area.model.promotion.DiscountType;
import com.thientri.book_area.model.user.Address;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.engagement.UserLibraryRepository;
import com.thientri.book_area.repository.order.CartItemRepository;
import com.thientri.book_area.repository.order.OrderRepository;
import com.thientri.book_area.repository.payment.PaymentRepository;
import com.thientri.book_area.repository.promotion.CouponRepository;
import com.thientri.book_area.repository.user.AddressRepository;
import com.thientri.book_area.service.payment.SePayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
	private final CartItemRepository cartItemRepository;
	private final OrderRepository orderRepository;
	private final AddressRepository addressRepository;
	private final PaymentRepository paymentRepository;
	private final CouponRepository couponRepository;
	private final UserLibraryRepository libraryRepository;
	private final OrderMapper orderMapper;
	private final SePayService sePayService;

	@Transactional
	public CheckoutResponse checkout(User user, CreateOrderRequest request, String clientIp) {
		log.info("Tạo đơn hàng: userId={}, paymentMethod={}, addressId={}", user.getId(),
				request.getPaymentMethod(), request.getAddressId());
		List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
		if (cartItems.isEmpty())
			throw new BadRequestException("Giỏ hàng đang trống.");

		boolean physical = cartItems.stream().anyMatch(item -> "PHYSICAL".equals(item.getEdition().getFormat()));
		Address address = null;
		if (physical) {
			if (request.getAddressId() == null) {
				throw new BadRequestException("Vui lòng chọn địa chỉ nhận sách.");
			}
			address = addressRepository.findById(request.getAddressId())
					.filter(item -> item.getUser().getId().equals(user.getId()))
					.orElseThrow(() -> new BadRequestException("Địa chỉ giao hàng không hợp lệ."));
		}

		String method = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "";
		if (!List.of("SEPAY", "COD").contains(method)) {
			throw new BadRequestException("Phương thức thanh toán này chưa được hỗ trợ.");
		}
		if ("COD".equalsIgnoreCase(method)
				&& cartItems.stream().anyMatch(item -> !"PHYSICAL".equals(item.getEdition().getFormat()))) {
			throw new BadRequestException("Sách điện tử cần được thanh toán trực tuyến qua SePay.");
		}

		cartItems.forEach(this::validateCheckoutItem);

		BigDecimal subTotal = cartItems.stream()
				.map(item -> item.getEdition().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal discount = calculateDiscount(request.getCouponCode(), subTotal);
		BigDecimal shippingFee = physical ? BigDecimal.valueOf(30000) : BigDecimal.ZERO;

		Order order = Order.builder()
				.orderCode("BA" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
				.user(user).status("PENDING").recipientName(physical ? address.getRecipientName() : null)
				.recipientPhone(physical ? address.getRecipientPhone() : null)
				.shippingAddressLine(physical ? address.getAddressLine() : null)
				.shippingProvinceName(physical ? address.getProvinceName() : null)
				.shippingDistrictName(physical ? address.getDistrictName() : null)
				.shippingWardName(physical ? address.getWardName() : null).subTotal(subTotal).shippingFee(shippingFee)
				.discountAmount(discount).appliedCouponCode(normalizeCoupon(request.getCouponCode()))
				.totalAmount(subTotal.add(shippingFee).subtract(discount)).build();

		for (CartItem cartItem : cartItems) {
			BookEdition edition = cartItem.getEdition();
			validateAndReserve(edition, cartItem.getQuantity(), user);
			order.addOrderItem(OrderItem.builder().edition(edition).quantity(cartItem.getQuantity())
					.originalPrice(edition.getOriginalPrice()).price(edition.getSalePrice()).build());
		}
		orderRepository.save(order);

		Payment payment = paymentRepository.save(Payment.builder().order(order).paymentMethod(method)
				.amount(order.getTotalAmount()).status(PaymentStatus.PENDING).build());

		cartItemRepository.deleteAll(cartItems);

		if ("COD".equalsIgnoreCase(method)) {
			return CheckoutResponse.builder().orderCode(order.getOrderCode()).paymentStatus(payment.getStatus().name())
					.build();
		}
		return sePayService.checkoutResponse(payment);
	}

	private void validateCheckoutItem(CartItem item) {
		if (item.getEdition() == null) {
			throw new BadRequestException("Sản phẩm trong giỏ hàng không hợp lệ.");
		}
		if (item.getQuantity() == null || item.getQuantity() <= 0) {
			throw new BadRequestException("Số lượng sản phẩm phải lớn hơn 0.");
		}
		if (item.getEdition().getSalePrice() == null || item.getEdition().getSalePrice().signum() <= 0) {
			throw new BadRequestException("Giá bán của sách chưa hợp lệ. Vui lòng liên hệ quản trị viên.");
		}
	}

	@Transactional(readOnly = true)
	public Page<OrderResponse> history(User user, int page, int size) {
		return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size))
				.map(orderMapper::toOrderResponse);
	}

	@Transactional
	public Payment handleSePayWebhook(WebhookRequest request, String rawBody) {
		if (!"in".equalsIgnoreCase(request.getTransferType()))
			throw new BadRequestException("Webhook không phải giao dịch tiền vào.");
		if (!sePayService.isExpectedAccount(request.getAccountNumber()))
			throw new BadRequestException("Tài khoản nhận tiền không hợp lệ.");
		String orderCode = extractOrderCode(request);
		Payment payment = paymentRepository.findByOrderOrderCode(orderCode)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch."));
		if (!"SEPAY".equalsIgnoreCase(payment.getPaymentMethod()))
			throw new BadRequestException("Phương thức thanh toán không hợp lệ.");
		if (payment.getStatus() != PaymentStatus.PENDING)
			return payment;
		String transactionId = request.getId() == null ? request.getReferenceCode() : request.getId().toString();
		if (transactionId == null || transactionId.isBlank())
			throw new BadRequestException("Mã giao dịch SePay không hợp lệ.");
		paymentRepository.findByTransactionId(transactionId)
				.filter(existing -> !existing.getId().equals(payment.getId())).ifPresent(existing -> {
					throw new BadRequestException("Giao dịch SePay đã được xử lý.");
				});
		if (request.getTransferAmount() == null || request.getTransferAmount().compareTo(payment.getAmount()) != 0)
			throw new BadRequestException("Số tiền chuyển khoản không khớp.");
		payment.setTransactionId(transactionId);
		payment.setGatewayResponse(rawBody);
		payment.setStatus(PaymentStatus.SUCCESS);
		payment.setPaidAt(LocalDateTime.now());

		payment.getOrder().setStatus("CONFIRMED");
		grantDigitalBooks(payment.getOrder());
		return paymentRepository.save(payment);
	}

	@Transactional(readOnly = true)
	public PaymentStatusResponse paymentStatus(User user, String orderCode) {
		Payment payment = paymentRepository.findByOrderOrderCodeAndOrderUserId(orderCode, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch."));
		return sePayService.statusResponse(payment);
	}

	private String extractOrderCode(WebhookRequest request) {
		if (request.getCode() != null && request.getCode().matches("(?i)BA[A-Z0-9]+"))
			return request.getCode().toUpperCase();
		if (request.getContent() != null) {
			java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)\\bBA[A-Z0-9]+\\b")
					.matcher(request.getContent());
			if (matcher.find())
				return matcher.group().toUpperCase();
		}
		throw new BadRequestException("Không tìm thấy mã đơn hàng trong giao dịch.");
	}

	private void validateAndReserve(BookEdition edition, int quantity, User user) {
		if (!Boolean.TRUE.equals(edition.getIsActive()))
			throw new BadRequestException("Có sách không còn được bán.");
		if ("PHYSICAL".equals(edition.getFormat())) {
			if (edition.getStock() == null || edition.getStock() < quantity)
				throw new ConflictException("Số lượng sách trong kho không đủ.");
			edition.setStock(edition.getStock() - quantity);
		} else if (libraryRepository.existsByUserIdAndEditionId(user.getId(), edition.getId())) {
			throw new ConflictException("Bạn đã sở hữu sách điện tử này.");
		}
	}

	private void grantDigitalBooks(Order order) {
		order.getOrderItems().stream().map(OrderItem::getEdition)
				.filter(edition -> !"PHYSICAL".equals(edition.getFormat()))
				.filter(edition -> !libraryRepository.existsByUserIdAndEditionId(order.getUser().getId(),
						edition.getId()))
				.forEach(edition -> libraryRepository
						.save(UserLibrary.builder().user(order.getUser()).edition(edition).build()));
	}

	private void releaseReservation(Order order) {
		order.getOrderItems().stream().filter(item -> "PHYSICAL".equals(item.getEdition().getFormat()))
				.forEach(item -> item.getEdition().setStock(item.getEdition().getStock() + item.getQuantity()));
		if (order.getAppliedCouponCode() != null) {
			couponRepository.findByCode(order.getAppliedCouponCode())
					.ifPresent(coupon -> coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1)));
		}
	}

	private BigDecimal calculateDiscount(String code, BigDecimal subTotal) {
		if (code == null || code.isBlank())
			return BigDecimal.ZERO;
		Coupon coupon = couponRepository.findByCode(code.trim().toUpperCase())
				.orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại."));
		if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now()))
			throw new BadRequestException("Mã giảm giá đã hết hạn.");
		if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit())
			throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng.");
		if (subTotal.compareTo(coupon.getMinOrderValue()) < 0)
			throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu của mã giảm giá.");
		BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENTAGE
				? subTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100))
				: coupon.getDiscountValue();
		if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0)
			discount = coupon.getMaxDiscount();
		coupon.setUsedCount(coupon.getUsedCount() + 1);
		return discount.min(subTotal);
	}

	private String normalizeCoupon(String code) {
		return code == null || code.isBlank() ? null : code.trim().toUpperCase();
	}
}
