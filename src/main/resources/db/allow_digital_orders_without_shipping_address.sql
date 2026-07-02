-- Cho phép đơn hàng chỉ gồm sách điện tử không cần thông tin giao hàng.
-- Chạy một lần trên SQL Server trước khi triển khai phiên bản ứng dụng này.

ALTER TABLE orders ALTER COLUMN recipient_name NVARCHAR(255) NULL;
ALTER TABLE orders ALTER COLUMN recipient_phone NVARCHAR(20) NULL;
ALTER TABLE orders ALTER COLUMN shipping_address_line NVARCHAR(MAX) NULL;
ALTER TABLE orders ALTER COLUMN shipping_province_name NVARCHAR(100) NULL;
ALTER TABLE orders ALTER COLUMN shipping_district_name NVARCHAR(100) NULL;
ALTER TABLE orders ALTER COLUMN shipping_ward_name NVARCHAR(100) NULL;
