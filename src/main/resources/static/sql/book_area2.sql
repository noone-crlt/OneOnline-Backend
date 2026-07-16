-- ==========================================
-- TẠO VÀ SỬ DỤNG DATABASE
-- ==========================================
SET NAMES utf8mb4;

DROP DATABASE IF EXISTS book_area3;
CREATE DATABASE book_area3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE book_area3;

-- =========================
-- 1. NGƯỜI DÙNG & BẢO MẬT
-- =========================

-- 1.1 Bảng Phân Quyền (Roles)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 1.2 Bảng Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20) UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED', 'DELETED'))
);

-- 1.3 Bảng trung gian User - Role
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 1.4 Bảng Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expiry_date DATETIME,
    user_id BIGINT NOT NULL,
    refresh_token VARCHAR(255) UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    revoked TINYINT(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================
-- 2. ĐỊA CHỈ
-- =========================

CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    address_line TEXT NOT NULL,
    province_id VARCHAR(50) NOT NULL,
    district_id VARCHAR(50) NOT NULL,
    ward_id VARCHAR(50) NOT NULL,
    province_name VARCHAR(100) NOT NULL,
    district_name VARCHAR(100) NOT NULL,
    ward_name VARCHAR(100) NOT NULL,
    is_default TINYINT(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================
-- 3. DANH MỤC THÔNG TIN CƠ BẢN
-- =========================

CREATE TABLE publishers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE narrators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- =========================
-- 4. TÁC PHẨM SÁCH (Sách gốc)
-- =========================

CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    images TEXT, -- Lưu chuỗi JSON chứa mảng URL ảnh
    publisher_id BIGINT,
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES publishers(id)
);

CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_role VARCHAR(50) DEFAULT 'AUTHOR' CHECK (author_role IN ('AUTHOR', 'TRANSLATOR')),
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id)
);

CREATE TABLE book_categories (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- =========================
-- 5. PHIÊN BẢN SÁCH (Variant)
-- =========================

CREATE TABLE book_editions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    format VARCHAR(50) NOT NULL CHECK (format IN ('PHYSICAL', 'EBOOK_PDF', 'EBOOK_EPUB', 'AUDIOBOOK')),
    sku_code VARCHAR(100) UNIQUE NULL,
    original_price DECIMAL(18, 0) NULL,
    sale_price DECIMAL(18, 0) NOT NULL,
    stock INT NULL,
    is_active TINYINT(1) DEFAULT 1,
    cover_object_name VARCHAR(500) NULL,
    file_object_name VARCHAR(500) NULL,
    duration INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- =========================
-- 6. CHI TIẾT SÁCH NÓI
-- =========================

CREATE TABLE edition_narrators (
    edition_id BIGINT NOT NULL,
    narrator_id BIGINT NOT NULL,
    PRIMARY KEY (edition_id, narrator_id),
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE CASCADE,
    FOREIGN KEY (narrator_id) REFERENCES narrators(id)
);

CREATE TABLE edition_audio_chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    edition_id BIGINT NOT NULL,
    title VARCHAR(255),
    chapter_number INT NOT NULL DEFAULT 1,
    audio_file_name TEXT,
    duration INT,
    CONSTRAINT UQ_Edition_Chapter UNIQUE (edition_id, chapter_number),
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE CASCADE
);

-- =========================
-- 7. GIỎ HÀNG
-- =========================

CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    edition_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE CASCADE
);

-- =========================
-- 8. KHUYẾN MÃI
-- =========================

CREATE TABLE coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE,
    discount_type VARCHAR(20) CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(18, 0),
    max_discount DECIMAL(18, 0),
    min_order_value DECIMAL(18, 0) NOT NULL DEFAULT 0,
    usage_limit INT NULL,
    used_count INT DEFAULT 0,
    expiry_date DATETIME
);

CREATE TABLE user_coupons (
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    used TINYINT(1) DEFAULT 0,
    PRIMARY KEY (user_id, coupon_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
);

-- =========================
-- 9. ĐƠN HÀNG
-- =========================

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,

    -- Snapshot Địa chỉ
    recipient_name VARCHAR(255) NULL,
    recipient_phone VARCHAR(20) NULL,
    shipping_address_line TEXT NULL,
    shipping_province_name VARCHAR(100) NULL,
    shipping_district_name VARCHAR(100) NULL,
    shipping_ward_name VARCHAR(100) NULL,

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    tracking_code VARCHAR(100) NULL,

    -- Báo cáo Kế toán
    sub_total DECIMAL(18, 0) NOT NULL,
    shipping_fee DECIMAL(18, 0) NOT NULL,
    applied_coupon_code VARCHAR(50) NULL,
    discount_amount DECIMAL(18, 0) DEFAULT 0,
    total_amount DECIMAL(18, 0) NOT NULL,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE NO ACTION
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    edition_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    original_price DECIMAL(18, 0) NULL,
    price DECIMAL(18, 0) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE NO ACTION
);

CREATE TABLE order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50),
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- =========================
-- 10. THANH TOÁN
-- =========================

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(18, 0) NOT NULL,
    status VARCHAR(50) CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    transaction_id VARCHAR(255),
    gateway_response TEXT,
    paid_at DATETIME NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE NO ACTION
);

-- =========================
-- 11. TƯƠNG TÁC SẢN PHẨM & KHO
-- =========================

CREATE TABLE user_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    edition_id BIGINT NOT NULL,
    progress INT DEFAULT 0,
    acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE NO ACTION,
    UNIQUE (user_id, edition_id)
);

CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    rating INT,
    comment TEXT,
    is_approved TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE inventory_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    edition_id BIGINT NOT NULL,
    change_amount INT,
    reason VARCHAR(255),
    created_by BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE NO ACTION,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE NO ACTION
);

-- ==========================================
-- INDEX TỐI ƯU HIỆU SUẤT (MySQL Syntax)
-- ==========================================
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_slug ON books(slug);
CREATE INDEX idx_editions_book_id ON book_editions(book_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX idx_user_library_user_id ON user_library(user_id);
CREATE INDEX idx_payments_transaction ON payments(transaction_id);

-- ==========================================
-- DỮ LIỆU MẪU (Bỏ tiền tố N'')
-- ==========================================

INSERT INTO roles (name) VALUES
('ADMIN'), ('USER'), ('STAFF'), ('EDITOR'), ('GUEST');

INSERT INTO users (email, password, full_name, phone, status) VALUES
('admin@bookarea.vn', 'hashed_pw_1', 'Nguyễn Quản Trị', '0901234567', 'ACTIVE'),
('user1@gmail.com', 'hashed_pw_2', 'Trần Khách Hàng', '0912345678', 'ACTIVE'),
('user2@gmail.com', 'hashed_pw_3', 'Lê Độc Giả', '0923456789', 'ACTIVE'),
('user3@gmail.com', 'hashed_pw_4', 'Phạm Sách', '0934567890', 'INACTIVE'),
('user4@gmail.com', 'hashed_pw_5', 'Hoàng Vi Phạm', '0945678901', 'BANNED');

INSERT INTO publishers (name, description) VALUES
('NXB Trẻ', 'Nhà xuất bản dành cho giới trẻ'),
('NXB Kim Đồng', 'Chuyên sách thiếu nhi'),
('Nhã Nam', 'Công ty văn hóa truyền thông Nhã Nam'),
('Alpha Books', 'Chuyên sách kinh tế, quản trị'),
('NXB Hội Nhà Văn', 'Các tác phẩm văn học chọn lọc');

INSERT INTO authors (name) VALUES
('Nguyễn Nhật Ánh'), ('Haruki Murakami'), ('J.K. Rowling'), ('Thích Nhất Hạnh'), ('Vũ Trọng Phụng');

INSERT INTO categories (name) VALUES
('Tiểu thuyết'), ('Kinh tế - Kinh doanh'), ('Tâm lý - Kỹ năng sống'), ('Thiếu nhi'), ('Khoa học viễn tưởng');

INSERT INTO narrators (name) VALUES
('Giọng đọc Hà Nội 1'), ('Giọng đọc Sài Gòn 1'), ('MC Thanh Tùng'), ('Thu Thủy Voice'), ('AI Voice Studio');

INSERT INTO coupons (code, discount_type, discount_value, max_discount, min_order_value, usage_limit, expiry_date) VALUES
('WELCOME20', 'PERCENTAGE', 20, 50000, 100000, 100, '2026-12-31'),
('GIAM50K', 'FIXED_AMOUNT', 50000, 50000, 200000, 50, '2026-12-31'),
('FLASH10', 'PERCENTAGE', 10, 20000, 50000, 200, '2026-12-31');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 2), (4, 2), (5, 5);

INSERT INTO refresh_tokens (user_id, refresh_token, expiry_date, revoked) VALUES
(1, 'token_admin_123', '2027-01-01', 0),
(2, 'token_user1_456', '2027-01-01', 0);

INSERT INTO addresses (user_id, recipient_name, recipient_phone, address_line, province_id, district_id, ward_id, province_name, district_name, ward_name, is_default) VALUES
(1, 'Nguyễn Quản Trị', '0901234567', '123 Lê Lợi', '01', '001', '00001', 'Hà Nội', 'Quận Ba Đình', 'Phường Phúc Xá', 1),
(2, 'Trần Khách Hàng', '0912345678', '456 Nguyễn Huệ', '79', '760', '26734', 'Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', 1);

INSERT INTO books (title, slug, description, images, publisher_id, is_active) VALUES
('Mắt Biếc', 'mat-biec', 'Một câu chuyện tình buồn...', '["mat-biec-cover.jpg"]', 1, 1),
('Rừng Na Uy', 'rung-na-uy', 'Tiểu thuyết nổi tiếng của Haruki Murakami.', '["rung-na-uy-front.png"]', 3, 1),
('Harry Potter và Hòn đá Phù thủy', 'harry-potter-1', 'Phần 1 của series Harry Potter.', '["harry-potter-1.jpg"]', 1, 1),
('Hiểu về trái tim', 'hieu-ve-trai-tim', 'Sách tâm lý trị liệu.', '["hieu-ve-trai-tim.jpg"]', 4, 1),
('Số Đỏ', 'so-do', 'Tác phẩm trào phúng của Vũ Trọng Phụng.', '["so-do-cover.jpg"]', 5, 1);

INSERT INTO book_authors (book_id, author_id, author_role) VALUES
(1, 1, 'AUTHOR'), (2, 2, 'AUTHOR'), (3, 3, 'AUTHOR'), (4, 4, 'AUTHOR'), (5, 5, 'AUTHOR');

INSERT INTO book_categories (book_id, category_id) VALUES
(1, 1), (2, 1), (3, 4), (4, 3), (5, 1);

INSERT INTO book_editions (book_id, format, sku_code, original_price, sale_price, stock, cover_object_name, file_object_name, duration) VALUES
(1, 'PHYSICAL', 'PHY-MB-001', 100000, 80000, 50, 'sach/anhbia/mat-biec.png', NULL, NULL),
(2, 'EBOOK_PDF', 'PDF-RNU-001', 50000, 30000, NULL, 'sach/anhbia/rung-na-uy.png', 'sach/_rung-na-uy.pdf', NULL),
(3, 'AUDIOBOOK', 'AUD-HP1-001', 120000, 100000, NULL, 'sach/anhbia/harry-potter-va-hon-da-phu-thuy.png', NULL, 600),
(4, 'PHYSICAL', 'PHY-HVTT-001', 150000, 120000, 30, 'cover4.jpg', NULL, NULL),
(5, 'EBOOK_EPUB', 'EPUB-SD-001', 40000, 20000, NULL, 'cover5.jpg', NULL, NULL);

INSERT INTO edition_narrators (edition_id, narrator_id) VALUES
(3, 1), (3, 2);

INSERT INTO cart_items (user_id, edition_id, quantity) VALUES
(1, 1, 2), (2, 3, 1), (3, 2, 1), (4, 4, 3), (5, 5, 1);

INSERT INTO user_library (user_id, edition_id, progress) VALUES
(2, 2, 0), (2, 3, 1500), (3, 5, 0), (4, 2, 0), (5, 3, 200);

INSERT INTO orders (order_code, user_id, recipient_name, recipient_phone, shipping_address_line, shipping_province_name, shipping_district_name, shipping_ward_name, status, sub_total, shipping_fee, applied_coupon_code, discount_amount, total_amount) VALUES
('ORD-260630-001', 2, 'Trần Khách Hàng', '0912345678', '456 Nguyễn Huệ', 'Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', 'COMPLETED', 160000, 20000, NULL, 0, 180000),
('ORD-260630-002', 3, 'Lê Độc Giả', '0923456789', '789 Trần Hưng Đạo', 'Đà Nẵng', 'Quận Hải Châu', 'Phường Thạch Thang', 'PENDING', 30000, 0, NULL, 0, 30000),
('ORD-260630-003', 4, 'Phạm Sách', '0934567890', '101 Hùng Vương', 'Hồ Chí Minh', 'Quận 3', 'Phường Võ Thị Sáu', 'CONFIRMED', 100000, 0, 'WELCOME20', 20000, 80000),
('ORD-260630-004', 5, 'Hoàng Vi Phạm', '0945678901', '202 Lý Thái Tổ', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Đào', 'CANCELLED', 240000, 30000, NULL, 0, 270000),
('ORD-260630-005', 2, 'Trần Khách Hàng 2', '0912345678', '123 Khác', 'Đồng Nai', 'Biên Hòa', 'Tân Phong', 'SHIPPING', 20000, 0, NULL, 0, 20000);

INSERT INTO order_items (order_id, edition_id, quantity, original_price, price) VALUES
(1, 1, 2, 100000, 80000), (2, 2, 1, 50000, 30000), (3, 3, 1, 120000, 100000), (4, 4, 2, 150000, 120000), (5, 5, 1, 40000, 20000);

INSERT INTO payments (order_id, payment_method, amount, status, transaction_id) VALUES
(1, 'COD', 180000, 'SUCCESS', 'TXN-COD-001'),
(2, 'SEPAY', 30000, 'PENDING', 'TXN-VNP-002'),
(3, 'MoMo', 80000, 'SUCCESS', 'TXN-MOM-003'),
(4, 'ZaloPay', 270000, 'REFUNDED', 'TXN-ZAL-004'),
(5, 'Credit Card', 20000, 'SUCCESS', 'TXN-CRE-005');

-- ==========================================
-- CÂU LỆNH KIỂM TRA BẠN YÊU CẦU
-- ==========================================
