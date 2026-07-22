package com.thientri.book_area.service.notification.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.thientri.book_area.model.order.Order;
import com.thientri.book_area.model.order.OrderItem;
import com.thientri.book_area.service.notification.IEmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username:}")
	private String fromEmail;

	@Override
	@Async
	public void sendInvoiceEmail(Order order) {
		if (fromEmail == null || fromEmail.isBlank()) {
			log.warn("Chưa cấu hình spring.mail.username. Không thể gửi email hóa đơn.");
			return;
		}

		String recipientEmail = order.getUser().getEmail();
		if (recipientEmail == null || recipientEmail.isBlank()) {
			log.warn("Không tìm thấy email của khách hàng cho đơn hàng: {}", order.getOrderCode());
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(recipientEmail);
			helper.setSubject("Hóa đơn thanh toán đơn hàng #" + order.getOrderCode() + " - OneOnline");

			String htmlContent = buildInvoiceHtml(order);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			log.info("Đã gửi email hóa đơn thành công cho đơn hàng: {} tới {}", order.getOrderCode(), recipientEmail);
		} catch (Exception e) {
			log.error("Lỗi khi gửi email hóa đơn cho đơn hàng: " + order.getOrderCode(), e);
		}
	}

	private String buildInvoiceHtml(Order order) {
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		StringBuilder itemsHtml = new StringBuilder();
		boolean hasDigitalItems = false;

		for (OrderItem item : order.getOrderItems()) {
			String formatName = translateFormat(item.getEdition().getFormat());
			if (isDigital(item.getEdition().getFormat())) {
				hasDigitalItems = true;
			}

			BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

			itemsHtml.append("<tr>")
					.append("<td style='padding: 12px; border-bottom: 1px solid #e4e4e7; font-weight: 500;'>")
					.append(item.getEdition().getBook().getTitle()).append("</td>")
					.append("<td style='padding: 12px; border-bottom: 1px solid #e4e4e7; color: #71717a;'>")
					.append(formatName).append("</td>")
					.append("<td style='padding: 12px; border-bottom: 1px solid #e4e4e7; text-align: right;'>")
					.append(currencyFormatter.format(item.getPrice())).append("</td>")
					.append("<td style='padding: 12px; border-bottom: 1px solid #e4e4e7; text-align: center;'>")
					.append(item.getQuantity()).append("</td>")
					.append("<td style='padding: 12px; border-bottom: 1px solid #e4e4e7; text-align: right; font-weight: 600;'>")
					.append(currencyFormatter.format(itemTotal)).append("</td>")
					.append("</tr>");
		}

		String shippingInfo = "";
		if (order.getShippingAddressLine() != null && !order.getShippingAddressLine().isBlank()) {
			shippingInfo = "<div style='background-color: #f4f4f5; padding: 16px; border-radius: 8px; margin-bottom: 24px;'>"
					+ "<h3 style='margin-top: 0; color: #18181b;'>Thông tin giao hàng</h3>"
					+ "<p style='margin: 4px 0; color: #4b5563;'><strong>Người nhận:</strong> " + order.getRecipientName() + " (" + order.getRecipientPhone() + ")</p>"
					+ "<p style='margin: 4px 0; color: #4b5563;'><strong>Địa chỉ:</strong> " + order.getShippingAddressLine() + ", " + order.getShippingWardName() + ", " + order.getShippingDistrictName() + ", " + order.getShippingProvinceName() + "</p>"
					+ "</div>";
		}

		String digitalGuide = "";
		if (hasDigitalItems) {
			digitalGuide = "<div style='border: 1px dashed #10b981; background-color: #f0fdf4; padding: 16px; border-radius: 8px; margin-bottom: 24px; color: #14532d;'>"
					+ "<h3 style='margin-top: 0; color: #15803d;'>📖 Hướng dẫn đọc sách trực tuyến</h3>"
					+ "<p style='margin: 4px 0; font-size: 0.95rem;'>Đơn hàng của bạn có chứa sách điện tử / sách nói. Bạn có thể đăng nhập vào tài khoản trên website <strong>OneOnline</strong>, truy cập mục <strong>Thư viện cá nhân</strong> để đọc trực tuyến bất kỳ lúc nào.</p>"
					+ "</div>";
		}

		BigDecimal subTotal = order.getSubTotal() != null ? order.getSubTotal() : order.getTotalAmount();
		BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
		BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
		BigDecimal total = order.getTotalAmount();

		return "<!DOCTYPE html>"
				+ "<html>"
				+ "<head>"
				+ "<meta charset='UTF-8'>"
				+ "</head>"
				+ "<body style=\"margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f9fafb; color: #1f2937; line-height: 1.6;\">"
				+ "<table width='100%' border='0' cellspacing='0' cellpadding='0' style='background-color: #f9fafb; padding: 24px 0;'>"
				+ "<tr>"
				+ "<td align='center'>"
				+ "<table width='600' border='0' cellspacing='0' cellpadding='0' style='background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>"
				+ "<!-- Header -->"
				+ "<tr>"
				+ "<td style='background-color: #09090b; padding: 32px; text-align: center;'>"
				+ "<h1 style='margin: 0; color: #ffffff; font-size: 1.75rem; letter-spacing: 0.1em; text-transform: uppercase;'>OneOnline</h1>"
				+ "<p style='margin: 8px 0 0 0; color: #a1a1aa; font-size: 0.9rem;'>Cảm ơn bạn đã mua sắm tại thư viện của chúng tôi</p>"
				+ "</td>"
				+ "</tr>"
				+ "<!-- Body -->"
				+ "<tr>"
				+ "<td style='padding: 32px;'>"
				+ "<h2 style='margin-top: 0; color: #111827; font-size: 1.25rem; border-bottom: 2px solid #f3f4f6; padding-bottom: 12px;'>Chi tiết hóa đơn đơn hàng #" + order.getOrderCode() + "</h2>"
				+ "<p style='color: #4b5563; font-size: 0.95rem;'>Xin chào <strong>" + (order.getUser().getFullName() != null ? order.getUser().getFullName() : order.getUser().getEmail()) + "</strong>,</p>"
				+ "<p style='color: #4b5563; font-size: 0.95rem;'>Chúng tôi đã xác nhận thanh toán thành công cho đơn hàng của bạn. Dưới đây là thông tin chi tiết:</p>"
				+ "<p style='font-size: 0.85rem; color: #6b7280; margin-bottom: 24px;'>Thời gian thanh toán: " + dateFormatter.format(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now()) + "</p>"
				
				+ shippingInfo
				+ digitalGuide

				+ "<table width='100%' border='0' cellspacing='0' cellpadding='0' style='margin-bottom: 24px; border-collapse: collapse; font-size: 0.95rem;'>"
				+ "<thead>"
				+ "<tr style='background-color: #f9fafb; border-bottom: 2px solid #e4e4e7;'>"
				+ "<th align='left' style='padding: 12px; font-weight: 700; color: #374151;'>Tác phẩm</th>"
				+ "<th align='left' style='padding: 12px; font-weight: 700; color: #374151;'>Định dạng</th>"
				+ "<th align='right' style='padding: 12px; font-weight: 700; color: #374151;'>Đơn giá</th>"
				+ "<th align='center' style='padding: 12px; font-weight: 700; color: #374151;'>SL</th>"
				+ "<th align='right' style='padding: 12px; font-weight: 700; color: #374151;'>Thành tiền</th>"
				+ "</tr>"
				+ "</thead>"
				+ "<tbody>"
				+ itemsHtml.toString()
				+ "</tbody>"
				+ "</table>"

				+ "<table width='260' border='0' cellspacing='0' cellpadding='0' align='right' style='font-size: 0.95rem; margin-bottom: 24px; color: #4b5563;'>"
				+ "<tr>"
				+ "<td style='padding: 6px 0;'>Tạm tính:</td>"
				+ "<td align='right' style='padding: 6px 0; font-weight: 500;'>" + currencyFormatter.format(subTotal) + "</td>"
				+ "</tr>"
				+ "<tr>"
				+ "<td style='padding: 6px 0;'>Phí giao hàng:</td>"
				+ "<td align='right' style='padding: 6px 0; font-weight: 500;'>" + currencyFormatter.format(shippingFee) + "</td>"
				+ "</tr>"
				+ "<tr>"
				+ "<td style='padding: 6px 0;'>Giảm giá:</td>"
				+ "<td align='right' style='padding: 6px 0; color: #ef4444; font-weight: 500;'>-" + currencyFormatter.format(discount) + "</td>"
				+ "</tr>"
				+ "<tr style='border-top: 1px solid #e4e4e7;'>"
				+ "<td style='padding: 12px 0 6px 0; font-weight: 700; color: #111827; font-size: 1.1rem;'>Tổng cộng:</td>"
				+ "<td align='right' style='padding: 12px 0 6px 0; font-weight: 700; color: #09090b; font-size: 1.1rem;'>" + currencyFormatter.format(total) + "</td>"
				+ "</tr>"
				+ "</table>"
				
				+ "<div style='clear: both;'></div>"
				
				+ "</td>"
				+ "</tr>"
				+ "<!-- Footer -->"
				+ "<tr>"
				+ "<td style='background-color: #f9fafb; padding: 24px; text-align: center; border-top: 1px solid #f3f4f6; font-size: 0.8rem; color: #9ca3af;'>"
				+ "<p style='margin: 0 0 8px 0;'>Mọi thắc mắc vui lòng phản hồi email này hoặc liên hệ hotline chăm sóc khách hàng.</p>"
				+ "<p style='margin: 0;'>© " + java.time.Year.now().getValue() + " OneOnline Library. All rights reserved.</p>"
				+ "</td>"
				+ "</tr>"
				+ "</table>"
				+ "</td>"
				+ "</tr>"
				+ "</table>"
				+ "</body>"
				+ "</html>";
	}

	private String translateFormat(String format) {
		if (format == null) return "Bản in";
		return switch (format.toUpperCase()) {
			case "EBOOK_PDF" -> "Ebook (PDF)";
			case "EBOOK_EPUB" -> "Ebook (EPUB)";
			case "AUDIOBOOK" -> "Sách nói (Audio)";
			case "PHYSICAL" -> "Sách giấy (Bản in)";
			default -> "Khác";
		};
	}

	private boolean isDigital(String format) {
		if (format == null) return false;
		String upper = format.toUpperCase();
		return upper.contains("EBOOK") || upper.contains("AUDIO");
	}
}
