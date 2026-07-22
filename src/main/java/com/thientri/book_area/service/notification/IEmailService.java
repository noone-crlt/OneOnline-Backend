package com.thientri.book_area.service.notification;

import com.thientri.book_area.model.order.Order;

public interface IEmailService {
	void sendInvoiceEmail(Order order);
}
