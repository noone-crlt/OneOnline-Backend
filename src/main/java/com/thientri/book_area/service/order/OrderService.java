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
import com.thientri.book_area.dto.response.order.CheckoutResponse;
import com.thientri.book_area.dto.response.order.OrderResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.mapper.OrderMapper;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.model.engagement.UserLibrary;
import com.thientri.book_area.model.order.Cart;
import com.thientri.book_area.model.order.CartItem;
import com.thientri.book_area.model.order.Order;
import com.thientri.book_area.model.order.OrderItem;
import com.thientri.book_area.model.order.OrderStatus;
import com.thientri.book_area.model.payment.Payment;
import com.thientri.book_area.model.payment.PaymentMethod;
import com.thientri.book_area.model.payment.PaymentStatus;
import com.thientri.book_area.model.promotion.Coupon;
import com.thientri.book_area.model.promotion.DiscountType;
import com.thientri.book_area.model.user.Address;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.engagement.UserLibraryRepository;
import com.thientri.book_area.repository.order.CartRepository;
import com.thientri.book_area.repository.order.OrderRepository;
import com.thientri.book_area.repository.order.OrderStatusRepository;
import com.thientri.book_area.repository.payment.PaymentMethodRepository;
import com.thientri.book_area.repository.payment.PaymentRepository;
import com.thientri.book_area.repository.promotion.CouponRepository;
import com.thientri.book_area.repository.user.AddressRepository;
import com.thientri.book_area.service.payment.VnPayService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final UserLibraryRepository libraryRepository;
    private final OrderMapper orderMapper;
    private final VnPayService vnPayService;

    @Transactional
    public CheckoutResponse checkout(User user, CreateOrderRequest request, String clientIp) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Giỏ hàng đang trống."));
        if (cart.getCartItems().isEmpty()) throw new BadRequestException("Giỏ hàng đang trống.");

        boolean physical = cart.getCartItems().stream()
                .anyMatch(item -> "PHYSICAL".equals(item.getEdition().getFormat()));
        Address address = null;
        if (physical) {
            if (request.getAddressId() == null) {
                throw new BadRequestException("Vui lòng chọn địa chỉ nhận sách.");
            }
            address = addressRepository.findById(request.getAddressId())
                    .filter(item -> item.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new BadRequestException("Địa chỉ giao hàng không hợp lệ."));
        }
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương thức thanh toán."));
        if (!List.of("VNPAY", "COD").contains(method.getName().toUpperCase())) {
            throw new BadRequestException("Phương thức thanh toán này chưa được hỗ trợ.");
        }
        if ("COD".equalsIgnoreCase(method.getName()) && cart.getCartItems().stream()
                .anyMatch(item -> !"PHYSICAL".equals(item.getEdition().getFormat()))) {
            throw new BadRequestException("Sách điện tử cần được thanh toán trực tuyến qua VNPay.");
        }

        BigDecimal subTotal = cart.getCartItems().stream()
                .map(item -> item.getEdition().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = calculateDiscount(request.getCouponCode(), subTotal);
        BigDecimal shippingFee = physical ? BigDecimal.valueOf(30000) : BigDecimal.ZERO;
        OrderStatus pending = orderStatusRepository.findByName("PENDING")
                .orElseThrow(() -> new IllegalStateException("Thiếu trạng thái đơn hàng PENDING."));

        Order order = Order.builder()
                .orderCode("BA" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
                .user(user).orderStatus(pending)
                .recipientName(physical ? address.getRecipientName() : null)
                .recipientPhone(physical ? address.getRecipientPhone() : null)
                .shippingAddressLine(physical ? address.getAddressLine() : null)
                .shippingProvinceName(physical ? address.getProvinceName() : null)
                .shippingDistrictName(physical ? address.getDistrictName() : null)
                .shippingWardName(physical ? address.getWardName() : null)
                .subTotal(subTotal).shippingFee(shippingFee).discountAmount(discount)
                .appliedCouponCode(normalizeCoupon(request.getCouponCode()))
                .totalAmount(subTotal.add(shippingFee).subtract(discount)).build();

        for (CartItem cartItem : cart.getCartItems()) {
            BookEdition edition = cartItem.getEdition();
            validateAndReserve(edition, cartItem.getQuantity(), user);
            order.addOrderItem(OrderItem.builder().edition(edition).quantity(cartItem.getQuantity())
                    .originalPrice(edition.getOriginalPrice()).price(edition.getSalePrice()).build());
        }
        orderRepository.save(order);

        Payment payment = paymentRepository.save(Payment.builder().order(order).paymentMethod(method)
                .amount(order.getTotalAmount()).status(PaymentStatus.PENDING).build());
        cart.getCartItems().clear();
        cartRepository.save(cart);

        if ("COD".equalsIgnoreCase(method.getName())) {
            return CheckoutResponse.builder().orderCode(order.getOrderCode()).paymentStatus(payment.getStatus().name()).build();
        }
        return CheckoutResponse.builder().orderCode(order.getOrderCode()).paymentStatus(payment.getStatus().name())
                .paymentUrl(vnPayService.createPaymentUrl(order, clientIp)).build();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> history(User user, int page, int size) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size))
                .map(orderMapper::toOrderResponse);
    }

    @Transactional
    public Payment handleVnPayReturn(java.util.Map<String, String> params) {
        if (!vnPayService.verify(params)) throw new BadRequestException("Chữ ký VNPay không hợp lệ.");
        String orderCode = params.get("vnp_TxnRef");
        Payment payment = paymentRepository.findByOrderOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch."));
        if (payment.getStatus() != PaymentStatus.PENDING) return payment;

        BigDecimal paidAmount = new BigDecimal(params.getOrDefault("vnp_Amount", "0")).divide(BigDecimal.valueOf(100));
        boolean successful = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"))
                && paidAmount.compareTo(payment.getAmount()) == 0;
        payment.setTransactionId(params.get("vnp_TransactionNo"));
        payment.setGatewayResponse(params.toString());
        payment.setStatus(successful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        if (successful) {
            payment.setPaidAt(LocalDateTime.now());
            OrderStatus confirmed = orderStatusRepository.findByName("CONFIRMED")
                    .orElseThrow(() -> new IllegalStateException("Thiếu trạng thái đơn hàng CONFIRMED."));
            payment.getOrder().setOrderStatus(confirmed);
            grantDigitalBooks(payment.getOrder());
        } else {
            releaseReservation(payment.getOrder());
        }
        return paymentRepository.save(payment);
    }

    private void validateAndReserve(BookEdition edition, int quantity, User user) {
        if (!Boolean.TRUE.equals(edition.getIsActive())) throw new BadRequestException("Có sách không còn được bán.");
        if ("PHYSICAL".equals(edition.getFormat())) {
            if (edition.getStock() == null || edition.getStock() < quantity) throw new BadRequestException("Số lượng tồn kho không đủ.");
            edition.setStock(edition.getStock() - quantity);
        } else if (libraryRepository.existsByUserIdAndEditionId(user.getId(), edition.getId())) {
            throw new BadRequestException("Bạn đã sở hữu một sách điện tử trong giỏ hàng.");
        }
    }

    private void grantDigitalBooks(Order order) {
        order.getOrderItems().stream().map(OrderItem::getEdition)
                .filter(edition -> !"PHYSICAL".equals(edition.getFormat()))
                .filter(edition -> !libraryRepository.existsByUserIdAndEditionId(order.getUser().getId(), edition.getId()))
                .forEach(edition -> libraryRepository.save(UserLibrary.builder().user(order.getUser()).edition(edition).build()));
    }

    private void releaseReservation(Order order) {
        order.getOrderItems().stream()
                .filter(item -> "PHYSICAL".equals(item.getEdition().getFormat()))
                .forEach(item -> item.getEdition().setStock(item.getEdition().getStock() + item.getQuantity()));
        if (order.getAppliedCouponCode() != null) {
            couponRepository.findByCode(order.getAppliedCouponCode()).ifPresent(coupon ->
                    coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1)));
        }
    }

    private BigDecimal calculateDiscount(String code, BigDecimal subTotal) {
        if (code == null || code.isBlank()) return BigDecimal.ZERO;
        Coupon coupon = couponRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại."));
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) throw new BadRequestException("Mã giảm giá đã hết hạn.");
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng.");
        if (subTotal.compareTo(coupon.getMinOrderValue()) < 0) throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu của mã giảm giá.");
        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENTAGE
                ? subTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100)) : coupon.getDiscountValue();
        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) discount = coupon.getMaxDiscount();
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        return discount.min(subTotal);
    }

    private String normalizeCoupon(String code) { return code == null || code.isBlank() ? null : code.trim().toUpperCase(); }
}
