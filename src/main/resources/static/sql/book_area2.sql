USE master;
GO

-- Đã đồng bộ tên Database khớp với application.properties
IF DB_ID('book_area3') IS NOT NULL
BEGIN
    ALTER DATABASE book_area3 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE book_area3;
END
GO

CREATE DATABASE book_area3;
GO

USE book_area3;
GO

DROP DATABASE book_area3;

-- =========================
-- 1. NGƯỜI DÙNG & BẢO MẬT
-- =========================

-- 1.1 Bảng Phân Quyền (Roles)
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE -- Sẽ lưu các giá trị như 'ROLE_ADMIN', 'ROLE_USER'
);
GO

-- 1.2 Bảng Users (Đã loại bỏ role_name cứng nhắc)
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY, 
    email NVARCHAR(255) NOT NULL UNIQUE, 
    password NVARCHAR(255) NOT NULL,     
    full_name NVARCHAR(255),             
    phone NVARCHAR(20) UNIQUE,           
    created_at DATETIME2 DEFAULT SYSDATETIME(), 
    -- Giữ nguyên Soft-Delete, Java sẽ lo phần dọn rác
    status NVARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED', 'DELETED')) 
);
GO

-- 1.3 Bảng trung gian User - Role (N-N)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
GO

-- 1.4 Bảng Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY, 
    expiry_date DATETIME2,               
    user_id BIGINT NOT NULL,             
    refresh_token NVARCHAR(255) UNIQUE NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    revoked BIT DEFAULT 0,	-- Đánh dấu refresh token đã bị thu hồi
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE 
);
GO

-- =========================
-- 2. ĐỊA CHỈ 
-- =========================

CREATE TABLE addresses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,             
    recipient_name NVARCHAR(255) NOT NULL, 
    recipient_phone NVARCHAR(20) NOT NULL, 
    address_line NVARCHAR(MAX) NOT NULL,   
    province_id NVARCHAR(50) NOT NULL,     
    district_id NVARCHAR(50) NOT NULL,     
    ward_id NVARCHAR(50) NOT NULL,         
    province_name NVARCHAR(100) NOT NULL,  
    district_name NVARCHAR(100) NOT NULL, 
    ward_name NVARCHAR(100) NOT NULL,     
    is_default BIT DEFAULT 0,              
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

-- =========================
-- 3. DANH MỤC THÔNG TIN CƠ BẢN
-- =========================

CREATE TABLE publishers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,         
    description NVARCHAR(MAX),           
    created_at DATETIME2 DEFAULT SYSDATETIME()
);
GO

CREATE TABLE authors (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255)                   
);
GO

CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255)                   
);
GO

CREATE TABLE narrators (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255)                   
);
GO

-- =========================
-- 4. TÁC PHẨM SÁCH (Sách gốc)
-- =========================

CREATE TABLE books (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,        
    slug NVARCHAR(255) UNIQUE NOT NULL,  
    description NVARCHAR(MAX),           
    publisher_id BIGINT,                 
    is_active BIT DEFAULT 1,             
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (publisher_id) REFERENCES publishers(id)
);
GO

CREATE TABLE book_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    book_id BIGINT NOT NULL,             
    image_file_name NVARCHAR(MAX),       
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);
GO

CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_role NVARCHAR(50) DEFAULT 'AUTHOR' CHECK (author_role IN ('AUTHOR', 'TRANSLATOR')), 
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id)
);
GO

CREATE TABLE book_categories (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
GO

-- =========================
-- 5. PHIÊN BẢN SÁCH (Variant)
-- =========================

CREATE TABLE book_editions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    book_id BIGINT NOT NULL,             
    format NVARCHAR(50) NOT NULL CHECK (format IN ('PHYSICAL', 'EBOOK_PDF', 'EBOOK_EPUB', 'AUDIOBOOK')), 
    sku_code NVARCHAR(100) UNIQUE NULL,  
    original_price DECIMAL(18, 0) NULL,  
    sale_price DECIMAL(18, 0) NOT NULL,  
    stock INT NULL,                      
    is_active BIT DEFAULT 1,             
    cover_object_name NVARCHAR(500) NULL, 
    file_object_name NVARCHAR(500) NULL,  
    duration INT NULL,                   
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);
GO

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
GO

CREATE TABLE edition_audio_chapters (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    edition_id BIGINT NOT NULL,          
    title NVARCHAR(255),
    chapter_number INT NOT NULL DEFAULT 1,
    audio_file_name NVARCHAR(MAX),       
    duration INT,                        
    CONSTRAINT UQ_Edition_Chapter UNIQUE (edition_id, chapter_number),
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE CASCADE
);
GO

-- =========================
-- 7. GIỎ HÀNG
-- =========================

CREATE TABLE carts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT UNIQUE,               
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

CREATE TABLE cart_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id BIGINT NOT NULL,             
    edition_id BIGINT NOT NULL,          
    quantity INT NOT NULL DEFAULT 1,     
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE CASCADE
);
GO

-- =========================
-- 8. TRẠNG THÁI & KHUYẾN MÃI
-- =========================

CREATE TABLE order_status (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE    
);
GO

CREATE TABLE coupons (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) UNIQUE,            
    discount_type NVARCHAR(20) CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')), 
    discount_value DECIMAL(18, 0),       
    max_discount DECIMAL(18, 0),         
    min_order_value DECIMAL(18, 0) NOT NULL DEFAULT 0, 
    usage_limit INT NULL,                
    used_count INT DEFAULT 0,            
    expiry_date DATETIME2                
);
GO

CREATE TABLE user_coupons (
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    used BIT DEFAULT 0,                  
    PRIMARY KEY (user_id, coupon_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
);
GO

-- =========================
-- 9. ĐƠN HÀNG (Snapshot Kế Toán & Vận Chuyển)
-- =========================

CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_code VARCHAR(50) UNIQUE NOT NULL, 
    user_id BIGINT NOT NULL,             
    
    -- Snapshot Địa chỉ
    recipient_name NVARCHAR(255) NOT NULL, 
    recipient_phone NVARCHAR(20) NOT NULL,
    shipping_address_line NVARCHAR(MAX) NOT NULL,
    shipping_province_name NVARCHAR(100) NOT NULL, 
    shipping_district_name NVARCHAR(100) NOT NULL,
    shipping_ward_name NVARCHAR(100) NOT NULL,
    
    status_id BIGINT NOT NULL,           
    tracking_code NVARCHAR(100) NULL,    
    
    -- Báo cáo Kế toán
    sub_total DECIMAL(18, 0) NOT NULL,          
    shipping_fee DECIMAL(18, 0) NOT NULL,       
    applied_coupon_code NVARCHAR(50) NULL,      
    discount_amount DECIMAL(18, 0) DEFAULT 0,   
    total_amount DECIMAL(18, 0) NOT NULL,       
    
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    -- [ĐÃ KHÓA]: NO ACTION. Nếu có hóa đơn, tuyệt đối không được xóa User khỏi database (chỉ được Soft-delete)
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE NO ACTION,
    FOREIGN KEY (status_id) REFERENCES order_status(id)
);
GO

CREATE TABLE order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,            
    edition_id BIGINT NOT NULL,          
    quantity INT NOT NULL DEFAULT 1,     
    -- [ĐÃ KHÓA]: Bổ sung original_price để hóa đơn chi tiết luôn ghi nhớ "Khách đã được giảm giá gốc bao nhiêu"
    original_price DECIMAL(18, 0) NULL,  
    price DECIMAL(18, 0) NOT NULL,       
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    -- [ĐÃ KHÓA]: NO ACTION. Không cho phép xóa Edition nếu nó đã từng nằm trong 1 đơn hàng (để đối soát doanh thu)
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE NO ACTION
);
GO

CREATE TABLE order_status_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status NVARCHAR(50),                 
    changed_at DATETIME2 DEFAULT SYSDATETIME(), 
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
GO

-- =========================
-- 10. THANH TOÁN
-- =========================

CREATE TABLE payment_methods (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) UNIQUE,            
    description NVARCHAR(MAX)
);
GO

CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,            
    payment_method_id BIGINT NOT NULL,   
    amount DECIMAL(18, 0) NOT NULL,      
    status NVARCHAR(50) CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')), 
    transaction_id NVARCHAR(255),        
    gateway_response NVARCHAR(MAX),      
    paid_at DATETIME2 NULL,              
    -- [ĐÃ KHÓA]: NO ACTION. Bảo vệ chứng từ thanh toán khỏi việc vô tình xóa đơn hàng
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE NO ACTION,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);
GO

-- =========================
-- 11. TƯƠNG TÁC SẢN PHẨM & KHO
-- =========================

CREATE TABLE user_library (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,             
    edition_id BIGINT NOT NULL,          
    acquired_at DATETIME2 DEFAULT SYSDATETIME(), 
    -- Nếu user tự nguyện hủy/xóa trắng tài khoản cá nhân, thư viện của họ đi theo
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    -- [ĐÃ KHÓA CỰC KỲ QUAN TRỌNG]: NO ACTION. Ngăn Admin xóa Ebook và làm mất quyền truy cập của khách hàng
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE NO ACTION,
    UNIQUE (user_id, edition_id)         
);
GO

CREATE TABLE listen_progress (
    id BIGINT IDENTITY(1,1) PRIMARY KEY, 
    user_id BIGINT NOT NULL,
    edition_id BIGINT NOT NULL,
    progress INT,                        
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    -- [ĐÃ KHÓA]: NO ACTION
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE NO ACTION,
    UNIQUE (user_id, edition_id)
);
GO

CREATE TABLE reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,             
    book_id BIGINT NOT NULL,             
    rating INT,                          
    comment NVARCHAR(MAX),               
    is_approved BIT DEFAULT 0,           
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    UNIQUE (user_id, book_id),           
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CHECK (rating BETWEEN 1 AND 5)       
);
GO

CREATE TABLE inventory_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    edition_id BIGINT NOT NULL,          
    change_amount INT,                   
    reason NVARCHAR(255),                
    created_by BIGINT NULL,              
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    -- [ĐÃ KHÓA]: NO ACTION. Bảo toàn lịch sử kho bãi
    FOREIGN KEY (edition_id) REFERENCES book_editions(id) ON DELETE NO ACTION,
    -- NO ACTION đối với nhân viên thao tác
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE NO ACTION
);
GO

-- =========================
-- INDEX TỐI ƯU HIỆU SUẤT
-- =========================
CREATE INDEX idx_books_title ON books(title); 
CREATE INDEX idx_books_slug ON books(slug); 
CREATE INDEX idx_editions_book_id ON book_editions(book_id); 
CREATE INDEX idx_orders_user_id ON orders(user_id); 
CREATE INDEX idx_order_items_order_id ON order_items(order_id); 
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id); 
CREATE INDEX idx_user_library_user_id ON user_library(user_id); 
CREATE INDEX idx_payments_transaction ON payments(transaction_id); 
GO


USE book_area3;
GO

-- ==========================================
-- BƯỚC 1: CÁC BẢNG ĐỘC LẬP (KHÔNG CÓ FOREIGN KEY)
-- ==========================================

-- 1. roles
INSERT INTO roles (name) VALUES 
('ADMIN'), ('USER'), ('STAFF'), ('EDITOR'), ('GUEST');
GO

-- 2. users
INSERT INTO users (email, password, full_name, phone, status) VALUES 
('admin@bookarea.vn', 'hashed_pw_1', N'Nguyễn Quản Trị', '0901234567', 'ACTIVE'),
('user1@gmail.com', 'hashed_pw_2', N'Trần Khách Hàng', '0912345678', 'ACTIVE'),
('user2@gmail.com', 'hashed_pw_3', N'Lê Độc Giả', '0923456789', 'ACTIVE'),
('user3@gmail.com', 'hashed_pw_4', N'Phạm Sách', '0934567890', 'INACTIVE'),
('user4@gmail.com', 'hashed_pw_5', N'Hoàng Vi Phạm', '0945678901', 'BANNED');
GO

-- 3. publishers
INSERT INTO publishers (name, description) VALUES 
(N'NXB Trẻ', N'Nhà xuất bản dành cho giới trẻ'),
(N'NXB Kim Đồng', N'Chuyên sách thiếu nhi'),
(N'Nhã Nam', N'Công ty văn hóa truyền thông Nhã Nam'),
(N'Alpha Books', N'Chuyên sách kinh tế, quản trị'),
(N'NXB Hội Nhà Văn', N'Các tác phẩm văn học chọn lọc');
GO

-- 4. authors
INSERT INTO authors (name) VALUES 
(N'Nguyễn Nhật Ánh'), (N'Haruki Murakami'), (N'J.K. Rowling'), (N'Thích Nhất Hạnh'), (N'Vũ Trọng Phụng');
GO

-- 5. categories
INSERT INTO categories (name) VALUES 
(N'Tiểu thuyết'), (N'Kinh tế - Kinh doanh'), (N'Tâm lý - Kỹ năng sống'), (N'Thiếu nhi'), (N'Khoa học viễn tưởng');
GO

-- 6. narrators
INSERT INTO narrators (name) VALUES 
(N'Giọng đọc Hà Nội 1'), (N'Giọng đọc Sài Gòn 1'), (N'MC Thanh Tùng'), (N'Thu Thủy Voice'), (N'AI Voice Studio');
GO

-- 7. order_status
INSERT INTO order_status (name) VALUES 
('PENDING'), ('CONFIRMED'), ('SHIPPING'), ('COMPLETED'), ('CANCELLED');
GO

-- 8. coupons
INSERT INTO coupons (code, discount_type, discount_value, max_discount, min_order_value, usage_limit, expiry_date) VALUES 
('WELCOME20', 'PERCENTAGE', 20, 50000, 100000, 100, '2026-12-31'),
('GIAM50K', 'FIXED_AMOUNT', 50000, 50000, 200000, 50, '2026-12-31'),
('FLASH10', 'PERCENTAGE', 10, 20000, 50000, 200, '2026-12-31'),
('FREESHIP', 'FIXED_AMOUNT', 30000, 30000, 150000, 500, '2026-12-31'),
('VIPMEMBER', 'PERCENTAGE', 30, 100000, 500000, 10, '2027-12-31');
GO

-- 9. payment_methods
INSERT INTO payment_methods (name, description) VALUES 
('COD', N'Thanh toán khi nhận hàng'),
('VNPay', N'Thanh toán qua cổng VNPay'),
('MoMo', N'Ví điện tử MoMo'),
('ZaloPay', N'Ví điện tử ZaloPay'),
('Credit Card', N'Thanh toán thẻ Visa/Mastercard');
GO


-- ==========================================
-- BƯỚC 2: CÁC BẢNG PHỤ THUỘC CẤP 1
-- ==========================================

-- 10. user_roles (User ID 1 -> Admin, ID 2-5 -> User)
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), (2, 2), (3, 2), (4, 2), (5, 5);
GO

-- 11. refresh_tokens
INSERT INTO refresh_tokens (user_id, refresh_token, expiry_date, revoked) VALUES 
(1, 'token_admin_123', '2027-01-01', 0),
(2, 'token_user1_456', '2027-01-01', 0),
(3, 'token_user2_789', '2027-01-01', 0),
(4, 'token_user3_012', '2026-05-01', 1),
(5, 'token_user4_345', '2026-05-01', 1);
GO

-- 12. addresses
INSERT INTO addresses (user_id, recipient_name, recipient_phone, address_line, province_id, district_id, ward_id, province_name, district_name, ward_name, is_default) VALUES 
(1, N'Nguyễn Quản Trị', '0901234567', N'123 Lê Lợi', '01', '001', '00001', N'Hà Nội', N'Quận Ba Đình', N'Phường Phúc Xá', 1),
(2, N'Trần Khách Hàng', '0912345678', N'456 Nguyễn Huệ', '79', '760', '26734', N'Hồ Chí Minh', N'Quận 1', N'Phường Bến Nghé', 1),
(3, N'Lê Độc Giả', '0923456789', N'789 Trần Hưng Đạo', '48', '490', '20194', N'Đà Nẵng', N'Quận Hải Châu', N'Phường Thạch Thang', 1),
(4, N'Phạm Sách', '0934567890', N'101 Hùng Vương', '79', '761', '26875', N'Hồ Chí Minh', N'Quận 3', N'Phường Võ Thị Sáu', 1),
(5, N'Hoàng Vi Phạm', '0945678901', N'202 Lý Thái Tổ', '01', '002', '00040', N'Hà Nội', N'Quận Hoàn Kiếm', N'Phường Hàng Đào', 1);
GO

-- 13. user_coupons
INSERT INTO user_coupons (user_id, coupon_id, used) VALUES 
(2, 1, 0), (2, 2, 1), (3, 3, 0), (4, 4, 1), (5, 5, 0);
GO

-- 14. books
INSERT INTO books (title, slug, description, publisher_id, is_active) VALUES 
(N'Mắt Biếc', 'mat-biec', N'Một câu chuyện tình buồn...', 1, 1),
(N'Rừng Na Uy', 'rung-na-uy', N'Tiểu thuyết nổi tiếng của Haruki Murakami.', 3, 1),
(N'Harry Potter và Hòn đá Phù thủy', 'harry-potter-1', N'Phần 1 của series Harry Potter.', 1, 1),
(N'Hiểu về trái tim', 'hieu-ve-trai-tim', N'Sách tâm lý trị liệu.', 4, 1),
(N'Số Đỏ', 'so-do', N'Tác phẩm trào phúng của Vũ Trọng Phụng.', 5, 1);
GO

-- 15. carts
INSERT INTO carts (user_id) VALUES 
(1), (2), (3), (4), (5);
GO


-- ==========================================
-- BƯỚC 3: CÁC BẢNG PHỤ THUỘC CẤP 2 (Từ Books)
-- ==========================================

-- 16. book_images
INSERT INTO book_images (book_id, image_file_name) VALUES 
(1, 'mat-biec-cover.jpg'),
(2, 'rung-na-uy-front.png'),
(3, 'harry-potter-1.jpg'),
(4, 'hieu-ve-trai-tim.jpg'),
(5, 'so-do-cover.jpg');
GO

-- 17. book_authors
INSERT INTO book_authors (book_id, author_id, author_role) VALUES 
(1, 1, 'AUTHOR'),
(2, 2, 'AUTHOR'),
(3, 3, 'AUTHOR'),
(4, 4, 'AUTHOR'),
(5, 5, 'AUTHOR');
GO

-- 18. book_categories
INSERT INTO book_categories (book_id, category_id) VALUES 
(1, 1), (2, 1), (3, 4), (4, 3), (5, 1);
GO

-- 19. book_editions (Tạo đa dạng định dạng: Vật lý, PDF, Audio)
INSERT INTO book_editions (book_id, format, sku_code, original_price, sale_price, stock, cover_object_name, file_object_name, duration) VALUES 
(1, 'PHYSICAL', 'PHY-MB-001', 100000, 80000, 50, 'sach/anhbia/matbiec.png', NULL, 'sach/_matbiec.pdf'),
(2, 'EBOOK_PDF', 'PDF-RNU-001', 50000, 30000, NULL, 'sach/anhbia/rungnauy.png', 'sach/_rung-na-uy.pdf', NULL),
(3, 'AUDIOBOOK', 'AUD-HP1-001', 120000, 100000, NULL, 'sach/anhbia/harrypotter.png', NULL, 600),
(4, 'PHYSICAL', 'PHY-HVTT-001', 150000, 120000, 30, 'cover4.jpg', NULL, NULL),
(5, 'EBOOK_EPUB', 'EPUB-SD-001', 40000, 20000, NULL, 'cover5.jpg', NULL, NULL);
GO


-- ==========================================
-- BƯỚC 4: CÁC BẢNG PHỤ THUỘC CẤP 3 (Từ Book_editions)
-- ==========================================

-- 20. edition_narrators (Chỉ áp dụng cho Edition 3 là Audiobook)
INSERT INTO edition_narrators (edition_id, narrator_id) VALUES 
(3, 1), (3, 2); 
-- Để đủ 5 record, mượn tạm edition khác dù logic nghiệp vụ thường chỉ audiobook mới có người đọc, nhưng schema không cấm:
INSERT INTO edition_narrators (edition_id, narrator_id) VALUES 
(1, 3), (2, 4), (5, 5);
GO

-- 21. edition_audio_chapters (Cho Edition 3)
INSERT INTO edition_audio_chapters (edition_id, title, chapter_number, audio_file_name, duration) VALUES 
(3, N'Chương 1: Đứa bé sống sót', 1, 'hp1-chap1.mp3', 30),
(3, N'Chương 2: Tấm kính biến mất', 2, 'hp1-chap2.mp3', 35),
(3, N'Chương 3: Những lá thư không ai nhận', 3, 'hp1-chap3.mp3', 28),
(3, N'Chương 4: Người giữ khóa', 4, 'hp1-chap4.mp3', 40),
(3, N'Chương 5: Hẻm Xéo', 5, 'hp1-chap5.mp3', 45);
GO

-- 22. cart_items
INSERT INTO cart_items (cart_id, edition_id, quantity) VALUES 
(1, 1, 2), (2, 3, 1), (3, 2, 1), (4, 4, 3), (5, 5, 1);
GO

-- 23. user_library (Kho lưu trữ sách điện tử/audio của khách)
INSERT INTO user_library (user_id, edition_id) VALUES 
(2, 2), (2, 3), (3, 5), (4, 2), (5, 3);
GO

-- 24. listen_progress
INSERT INTO listen_progress (user_id, edition_id, progress) VALUES 
(2, 3, 1500), (5, 3, 200),
-- Cố tình thêm vào các bản ghi để đủ 5 dòng
(3, 3, 100), (4, 3, 500), (1, 3, 10);
GO

-- 25. inventory_logs
INSERT INTO inventory_logs (edition_id, change_amount, reason, created_by) VALUES 
(1, 100, N'Nhập kho ban đầu', 1),
(4, 50, N'Nhập kho đợt 1', 1),
(1, -2, N'Bán hàng đơn #1001', 1),
(4, -5, N'Hàng lỗi trả nhà cung cấp', 1),
(1, 10, N'Khách trả hàng', 1);
GO

-- 26. reviews
INSERT INTO reviews (user_id, book_id, rating, comment, is_approved) VALUES 
(2, 1, 5, N'Sách rất hay và cảm động', 1),
(3, 2, 4, N'Nội dung hơi buồn nhưng sâu sắc', 1),
(4, 3, 5, N'Series tuổi thơ không bao giờ chán', 0),
(5, 4, 4, N'Đọc để tĩnh tâm', 1),
(2, 5, 3, N'Văn phong trào phúng kinh điển', 1);
GO

-- 27. orders
INSERT INTO orders (order_code, user_id, recipient_name, recipient_phone, shipping_address_line, shipping_province_name, shipping_district_name, shipping_ward_name, status_id, sub_total, shipping_fee, applied_coupon_code, discount_amount, total_amount) VALUES 
('ORD-260630-001', 2, N'Trần Khách Hàng', '0912345678', N'456 Nguyễn Huệ', N'Hồ Chí Minh', N'Quận 1', N'Phường Bến Nghé', 4, 160000, 20000, NULL, 0, 180000),
('ORD-260630-002', 3, N'Lê Độc Giả', '0923456789', N'789 Trần Hưng Đạo', N'Đà Nẵng', N'Quận Hải Châu', N'Phường Thạch Thang', 1, 30000, 0, NULL, 0, 30000),
('ORD-260630-003', 4, N'Phạm Sách', '0934567890', N'101 Hùng Vương', N'Hồ Chí Minh', N'Quận 3', N'Phường Võ Thị Sáu', 2, 100000, 0, 'WELCOME20', 20000, 80000),
('ORD-260630-004', 5, N'Hoàng Vi Phạm', '0945678901', N'202 Lý Thái Tổ', N'Hà Nội', N'Quận Hoàn Kiếm', N'Phường Hàng Đào', 5, 240000, 30000, NULL, 0, 270000),
('ORD-260630-005', 2, N'Trần Khách Hàng 2', '0912345678', N'123 Khác', N'Đồng Nai', N'Biên Hòa', N'Tân Phong', 3, 20000, 0, NULL, 0, 20000);
GO


-- ==========================================
-- BƯỚC 5: CÁC BẢNG PHỤ THUỘC CẤP 4 (Từ Orders)
-- ==========================================

-- 28. order_items
INSERT INTO order_items (order_id, edition_id, quantity, original_price, price) VALUES 
(1, 1, 2, 100000, 80000),
(2, 2, 1, 50000, 30000),
(3, 3, 1, 120000, 100000),
(4, 4, 2, 150000, 120000),
(5, 5, 1, 40000, 20000);
GO

-- 29. order_status_history
INSERT INTO order_status_history (order_id, status) VALUES 
(1, 'PENDING'),
(1, 'CONFIRMED'),
(1, 'SHIPPING'),
(1, 'COMPLETED'),
(4, 'CANCELLED');
GO

-- 30. payments
INSERT INTO payments (order_id, payment_method_id, amount, status, transaction_id) VALUES 
(1, 1, 180000, 'SUCCESS', 'TXN-COD-001'),
(2, 2, 30000, 'PENDING', 'TXN-VNP-002'),
(3, 3, 80000, 'SUCCESS', 'TXN-MOM-003'),
(4, 4, 270000, 'REFUNDED', 'TXN-ZAL-004'),
(5, 5, 20000, 'SUCCESS', 'TXN-CRE-005');
GO

SELECT * FROM book_editions;
SELECT * FROM order_status_history;
select * from users;
select * from user_roles;
select * from roles