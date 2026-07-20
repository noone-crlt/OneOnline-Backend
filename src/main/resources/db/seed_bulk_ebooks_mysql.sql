-- Thêm hàng loạt sách PDF vào MySQL sau khi các tệp đã được tải lên MinIO.
-- Bucket: bucket-oneonline
-- PDF: sach/<tên-file>.pdf
-- Ảnh bìa: sach/anhbia/<tên-file>.(jpg|png)
--
-- Script có thể chạy nhiều lần mà không tạo trùng sách, phiên bản, tác giả
-- hoặc danh mục. Các slug và sku_code là khóa đối chiếu chính.

START TRANSACTION;

INSERT INTO publishers (name, description)
SELECT 'One Online', 'Nhà phát hành sách điện tử trên One Online'
WHERE NOT EXISTS (
    SELECT 1 FROM publishers WHERE name = 'One Online'
);

-- Bảo đảm các thể loại được dùng trong script tồn tại. Nếu database đã có
-- các thể loại này thì khối lệnh không thêm bản ghi mới.
INSERT INTO categories (name)
SELECT seed.name
FROM (
    SELECT 'Văn học' AS name
    UNION ALL SELECT 'Kinh tế'
    UNION ALL SELECT 'Kỹ năng sống'
    UNION ALL SELECT 'Trinh thám'
    UNION ALL SELECT 'Tâm lý'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE categories.name = seed.name
);

INSERT INTO authors (name)
SELECT 'Tác giả đang cập nhật'
WHERE NOT EXISTS (
    SELECT 1 FROM authors WHERE name = 'Tác giả đang cập nhật'
);

SET @publisher_id = (
    SELECT id FROM publishers WHERE name = 'One Online' ORDER BY id LIMIT 1
);
SET @author_id = (
    SELECT id FROM authors WHERE name = 'Tác giả đang cập nhật' ORDER BY id LIMIT 1
);

INSERT INTO books (title, slug, description, images, publisher_id, is_active)
SELECT seed.title,
       seed.slug,
       seed.description,
       NULL,
       @publisher_id,
       1
FROM (
    SELECT 'Biểu Tượng Thất Truyền' AS title, 'bieu-tuong-that-truyen' AS slug,
           'Phiên bản sách điện tử PDF.' AS description
    UNION ALL SELECT 'Cây Chuối Non Đi Giày Xanh', 'cay-chuoi-non-di-giay-xanh',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Chim Cổ Đỏ', 'chim-co-do',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Mật Mã Da Vinci', 'mat-ma-da-vinci',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Mưu Sát', 'muu-sat',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Ngày Xưa Có Một Chuyện Tình', 'ngay-xua-co-mot-chuyen-tinh',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Nghệ Thuật Tư Duy Chiến Lược', 'nghe-thuat-tu-duy-chien-luoc',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Nhà Giả Kim', 'nha-gia-kim',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Phía Sau Nghi Can X', 'phia-sau-nghi-can-x',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Sherlock Holmes Toàn Tập', 'sherlock-holmes-toan-tap',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Tết Ở Làng Địa Ngục', 'tet-o-lang-dia-nguc',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Thao Túng Tâm Lý', 'thao-tung-tam-ly',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Thế Giới Ngầm Của Ripley', 'the-gioi-ngam-cua-ripley',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Trường Ca Achilles', 'truong-ca-achilles',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Tư Duy Ngược Dịch Chuyển Thế Giới', 'tu-duy-nguoc-dich-chuyen-the-gioi',
           'Phiên bản sách điện tử PDF.'
    UNION ALL SELECT 'Tuổi Trẻ Đáng Giá Bao Nhiêu', 'tuoi-tre-dang-gia-bao-nhieu',
           'Phiên bản sách điện tử PDF.'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM books WHERE books.slug = seed.slug
);

INSERT IGNORE INTO book_authors (book_id, author_id)
SELECT books.id, @author_id
FROM books
WHERE books.slug IN (
    'bieu-tuong-that-truyen',
    'cay-chuoi-non-di-giay-xanh',
    'chim-co-do',
    'mat-ma-da-vinci',
    'muu-sat',
    'ngay-xua-co-mot-chuyen-tinh',
    'nghe-thuat-tu-duy-chien-luoc',
    'nha-gia-kim',
    'phia-sau-nghi-can-x',
    'sherlock-holmes-toan-tap',
    'tet-o-lang-dia-nguc',
    'thao-tung-tam-ly',
    'the-gioi-ngam-cua-ripley',
    'truong-ca-achilles',
    'tu-duy-nguoc-dich-chuyen-the-gioi',
    'tuoi-tre-dang-gia-bao-nhieu'
);

INSERT IGNORE INTO book_categories (book_id, category_id)
SELECT books.id, categories.id
FROM (
    SELECT 'bieu-tuong-that-truyen' AS slug, 'Trinh thám' AS category_name
    UNION ALL SELECT 'bieu-tuong-that-truyen', 'Văn học'
    UNION ALL SELECT 'cay-chuoi-non-di-giay-xanh', 'Văn học'
    UNION ALL SELECT 'chim-co-do', 'Trinh thám'
    UNION ALL SELECT 'chim-co-do', 'Văn học'
    UNION ALL SELECT 'mat-ma-da-vinci', 'Trinh thám'
    UNION ALL SELECT 'mat-ma-da-vinci', 'Văn học'
    UNION ALL SELECT 'muu-sat', 'Trinh thám'
    UNION ALL SELECT 'ngay-xua-co-mot-chuyen-tinh', 'Văn học'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc', 'Kinh tế'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc', 'Kỹ năng sống'
    UNION ALL SELECT 'nha-gia-kim', 'Văn học'
    UNION ALL SELECT 'nha-gia-kim', 'Kỹ năng sống'
    UNION ALL SELECT 'phia-sau-nghi-can-x', 'Trinh thám'
    UNION ALL SELECT 'phia-sau-nghi-can-x', 'Văn học'
    UNION ALL SELECT 'sherlock-holmes-toan-tap', 'Trinh thám'
    UNION ALL SELECT 'sherlock-holmes-toan-tap', 'Văn học'
    UNION ALL SELECT 'tet-o-lang-dia-nguc', 'Trinh thám'
    UNION ALL SELECT 'tet-o-lang-dia-nguc', 'Văn học'
    UNION ALL SELECT 'thao-tung-tam-ly', 'Tâm lý'
    UNION ALL SELECT 'thao-tung-tam-ly', 'Kỹ năng sống'
    UNION ALL SELECT 'the-gioi-ngam-cua-ripley', 'Trinh thám'
    UNION ALL SELECT 'the-gioi-ngam-cua-ripley', 'Văn học'
    UNION ALL SELECT 'truong-ca-achilles', 'Văn học'
    UNION ALL SELECT 'tu-duy-nguoc-dich-chuyen-the-gioi', 'Kỹ năng sống'
    UNION ALL SELECT 'tu-duy-nguoc-dich-chuyen-the-gioi', 'Tâm lý'
    UNION ALL SELECT 'tuoi-tre-dang-gia-bao-nhieu', 'Kỹ năng sống'
) AS seed
JOIN books ON books.slug = seed.slug
JOIN categories ON categories.name = seed.category_name;

INSERT INTO book_editions (
    book_id,
    format,
    sku_code,
    original_price,
    sale_price,
    stock,
    is_active,
    cover_object_name,
    file_object_name,
    duration
)
SELECT books.id,
       'EBOOK_PDF',
       seed.sku_code,
       seed.original_price,
       seed.sale_price,
       NULL,
       1,
       seed.cover_object_name,
       seed.file_object_name,
       NULL
FROM books
JOIN (
    SELECT 'bieu-tuong-that-truyen' AS slug, 'PDF-BTTT-001' AS sku_code,
           79000 AS original_price, 49000 AS sale_price,
           'sach/anhbia/bieu-tuong-that-truyen.jpg' AS cover_object_name,
           'sach/_bieu-tuong-that-truyen.pdf' AS file_object_name
    UNION ALL SELECT 'cay-chuoi-non-di-giay-xanh', 'PDF-CCNDGX-001', 69000, 39000,
           'sach/anhbia/cay-chuoi-non-di-giay-xanh.jpg', 'sach/_cay-chuoi-non-di-giay-xanh.pdf'
    UNION ALL SELECT 'chim-co-do', 'PDF-CCD-001', 79000, 49000,
           'sach/anhbia/chim-co-do.jpg', 'sach/_chim-co-do.pdf'
    UNION ALL SELECT 'mat-ma-da-vinci', 'PDF-MMDV-001', 89000, 59000,
           'sach/anhbia/mat-ma-da-vinci.jpg', 'sach/_mat-ma-da-vinci.pdf'
    UNION ALL SELECT 'muu-sat', 'PDF-MS-001', 79000, 49000,
           'sach/anhbia/muu-sat.jpg', 'sach/_muu-sat.pdf'
    UNION ALL SELECT 'ngay-xua-co-mot-chuyen-tinh', 'PDF-NXCMT-001', 69000, 39000,
           'sach/anhbia/ngay-xua-co-mot-chuyen-tinh.jpg', 'sach/_ngay-xua-co-mot-chuyen-tinh.pdf'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc', 'PDF-NTTDCL-001', 89000, 59000,
           'sach/anhbia/nghe-thuat-tu-duy-chien-luoc.jpg', 'sach/_nghe-thuat-tu-duy-chien-luoc.pdf'
    UNION ALL SELECT 'nha-gia-kim', 'PDF-NGK-001', 69000, 39000,
           'sach/anhbia/nha-gia-kim.png', 'sach/_nha-gia-kim.pdf'
    UNION ALL SELECT 'phia-sau-nghi-can-x', 'PDF-PSNCX-001', 79000, 49000,
           'sach/anhbia/phia-sau-nghi-can-x.jpg', 'sach/_phia-sau-nghi-can-x.pdf'
    UNION ALL SELECT 'sherlock-holmes-toan-tap', 'PDF-SHTT-001', 99000, 69000,
           'sach/anhbia/sherlock-holmes-toan-tap.jpg', 'sach/_sherlock-holmes-toan-tap.pdf'
    UNION ALL SELECT 'tet-o-lang-dia-nguc', 'PDF-TOLDN-001', 79000, 49000,
           'sach/anhbia/tet-o-lang-dia-nguc.jpg', 'sach/_tet-o-lang-dia-nguc.pdf'
    UNION ALL SELECT 'thao-tung-tam-ly', 'PDF-TTTL-001', 79000, 49000,
           'sach/anhbia/thao-tung-tam-ly.jpg', 'sach/_thao-tung-tam-ly.pdf'
    UNION ALL SELECT 'the-gioi-ngam-cua-ripley', 'PDF-TGNCR-001', 89000, 59000,
           'sach/anhbia/the-gioi-ngam-cua-ripley.jpg', 'sach/_the-gioi-ngam-cua-ripley.pdf'
    UNION ALL SELECT 'truong-ca-achilles', 'PDF-TCA-001', 89000, 59000,
           'sach/anhbia/truong-ca-achilles.jpg', 'sach/_truong-ca-achilles.pdf'
    UNION ALL SELECT 'tu-duy-nguoc-dich-chuyen-the-gioi', 'PDF-TDNDCTG-001', 89000, 59000,
           'sach/anhbia/tu-duy-nguoc-dich-chuyen-the-gioi.jpg', 'sach/_tu-duy-nguoc-dich-chuyen-the-gioi.pdf'
    UNION ALL SELECT 'tuoi-tre-dang-gia-bao-nhieu', 'PDF-TTDGBN-001', 69000, 39000,
           'sach/anhbia/tuoi-tre-dang-gia-bao-nhieu.png', 'sach/_tuoi-tre-dang-gia-bao-nhieu.pdf'
) AS seed ON seed.slug = books.slug
LEFT JOIN book_editions ON book_editions.sku_code = seed.sku_code
WHERE book_editions.id IS NULL;

COMMIT;

-- Kiểm tra kết quả sau khi chạy:
SELECT books.id,
       books.title,
       books.slug,
       book_editions.sku_code,
       book_editions.cover_object_name,
       book_editions.file_object_name
FROM books
JOIN book_editions ON book_editions.book_id = books.id
WHERE book_editions.sku_code LIKE 'PDF-%-001'
ORDER BY books.id DESC;
