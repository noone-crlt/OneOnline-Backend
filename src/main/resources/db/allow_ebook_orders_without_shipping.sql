-- Ebook không có địa chỉ giao hàng. Các cột này chỉ bắt buộc ở tầng nghiệp vụ
-- khi giỏ hàng có phiên bản PHYSICAL.
USE book_area3;
GO

ALTER TABLE orders ALTER COLUMN recipient_name NVARCHAR(255) NULL;
ALTER TABLE orders ALTER COLUMN recipient_phone NVARCHAR(20) NULL;
ALTER TABLE orders ALTER COLUMN shipping_address_line NVARCHAR(MAX) NULL;
ALTER TABLE orders ALTER COLUMN shipping_province_name NVARCHAR(100) NULL;
ALTER TABLE orders ALTER COLUMN shipping_district_name NVARCHAR(100) NULL;
ALTER TABLE orders ALTER COLUMN shipping_ward_name NVARCHAR(100) NULL;
GO
