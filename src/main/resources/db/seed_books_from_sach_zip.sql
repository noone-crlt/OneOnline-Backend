-- seed_books_from_sach_zip.sql
-- Sinh dựa trên các file trong sach (1).zip.
-- Dùng cho schema book_area3 / OneOnline.
-- Có thể chạy lại nhiều lần: đối chiếu theo books.slug và book_editions.sku_code.

START TRANSACTION;

-- 1) Publisher
INSERT INTO publishers (name, description)
SELECT 'One Online', 'Nhà phát hành sách điện tử trên One Online'
WHERE NOT EXISTS (
    SELECT 1 FROM publishers WHERE name = 'One Online'
);

SET @publisher_id = (
    SELECT id FROM publishers WHERE name = 'One Online' ORDER BY id LIMIT 1
);

-- 2) Categories
INSERT INTO categories (name)
SELECT seed.name
FROM (
    SELECT 'Văn học' AS name
    UNION ALL SELECT 'Trinh thám'
    UNION ALL SELECT 'Kỹ năng sống'
    UNION ALL SELECT 'Tâm lý'
    UNION ALL SELECT 'Lịch sử'
    UNION ALL SELECT 'Kinh tế'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM categories c WHERE c.name = seed.name
);

-- 3) Authors
INSERT INTO authors (name)
SELECT seed.name
FROM (
    SELECT 'Dan Brown' AS name
    UNION ALL SELECT 'Nguyễn Nhật Ánh'
    UNION ALL SELECT 'Jo Nesbø'
    UNION ALL SELECT 'Viktor E. Frankl'
    UNION ALL SELECT 'J.K. Rowling'
    UNION ALL SELECT 'Chu Lai'
    UNION ALL SELECT 'Tác giả đang cập nhật'
    UNION ALL SELECT 'Avinash K. Dixit'
    UNION ALL SELECT 'Barry J. Nalebuff'
    UNION ALL SELECT 'Paulo Coelho'
    UNION ALL SELECT 'Higashino Keigo'
    UNION ALL SELECT 'Haruki Murakami'
    UNION ALL SELECT 'Yuval Noah Harari'
    UNION ALL SELECT 'Arthur Conan Doyle'
    UNION ALL SELECT 'Ngô Thừa Ân'
    UNION ALL SELECT 'Thảo Trang'
    UNION ALL SELECT 'Shannon Thomas'
    UNION ALL SELECT 'Patricia Highsmith'
    UNION ALL SELECT 'Madeline Miller'
    UNION ALL SELECT 'Adam Grant'
    UNION ALL SELECT 'Rosie Nguyễn'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM authors a WHERE a.name = seed.name
);

-- 4) Books
INSERT INTO books (title, slug, description, images, publisher_id, is_active)
SELECT seed.title, seed.slug, seed.description, NULL, @publisher_id, 1
FROM (
    SELECT 'Biểu Tượng Thất Truyền' title, 'bieu-tuong-that-truyen' slug,
           'Tiểu thuyết trinh thám của Dan Brown.' description
    UNION ALL SELECT 'Cây Chuối Non Đi Giày Xanh', 'cay-chuoi-non-di-giay-xanh',
           'Tác phẩm văn học của Nguyễn Nhật Ánh.'
    UNION ALL SELECT 'Chim Cổ Đỏ', 'chim-co-do',
           'Tiểu thuyết trinh thám của Jo Nesbø.'
    UNION ALL SELECT 'Cho Tôi Xin Một Vé Đi Tuổi Thơ', 'cho-toi-xin-mot-ve-di-tuoi-tho',
           'Tác phẩm văn học của Nguyễn Nhật Ánh.'
    UNION ALL SELECT 'Đi Tìm Lẽ Sống', 'di-tim-le-song',
           'Tác phẩm về ý nghĩa cuộc sống và tâm lý học của Viktor E. Frankl.'
    UNION ALL SELECT 'Harry Potter Và Hòn Đá Phù Thủy', 'harry-potter-va-hon-da-phu-thuy',
           'Phần đầu của loạt truyện Harry Potter.'
    UNION ALL SELECT 'Mắt Biếc', 'mat-biec',
           'Tác phẩm văn học của Nguyễn Nhật Ánh.'
    UNION ALL SELECT 'Mật Mã Da Vinci', 'mat-ma-da-vinci',
           'Tiểu thuyết trinh thám của Dan Brown.'
    UNION ALL SELECT 'Mưa Đỏ', 'mua-do',
           'Tiểu thuyết chiến tranh Việt Nam.'
    UNION ALL SELECT 'Mưu Sát', 'muu-sat',
           'Tiểu thuyết trinh thám.'
    UNION ALL SELECT 'Ngày Xưa Có Một Chuyện Tình', 'ngay-xua-co-mot-chuyen-tinh',
           'Tác phẩm văn học của Nguyễn Nhật Ánh.'
    UNION ALL SELECT 'Nghệ Thuật Tư Duy Chiến Lược', 'nghe-thuat-tu-duy-chien-luoc',
           'Sách về tư duy chiến lược và lý thuyết trò chơi.'
    UNION ALL SELECT 'Nhà Giả Kim', 'nha-gia-kim',
           'Tiểu thuyết của Paulo Coelho.'
    UNION ALL SELECT 'Phía Sau Nghi Can X', 'phia-sau-nghi-can-x',
           'Tiểu thuyết trinh thám của Higashino Keigo.'
    UNION ALL SELECT 'Rừng Na Uy', 'rung-na-uy',
           'Tiểu thuyết của Haruki Murakami.'
    UNION ALL SELECT 'Sapiens - Lược Sử Loài Người', 'sapiens-luoc-su-loai-nguoi',
           'Tác phẩm lịch sử của Yuval Noah Harari.'
    UNION ALL SELECT 'Sherlock Holmes Toàn Tập', 'sherlock-holmes-toan-tap',
           'Tuyển tập truyện trinh thám Sherlock Holmes.'
    UNION ALL SELECT 'Tây Du Ký', 'tay-du-ky',
           'Tác phẩm văn học kinh điển của Ngô Thừa Ân.'
    UNION ALL SELECT 'Tết Ở Làng Địa Ngục', 'tet-o-lang-dia-nguc',
           'Tiểu thuyết kinh dị và trinh thám của Thảo Trang.'
    UNION ALL SELECT 'Thao Túng Tâm Lý', 'thao-tung-tam-ly',
           'Sách về tâm lý và nhận diện hành vi thao túng.'
    UNION ALL SELECT 'Thế Giới Ngầm Của Ripley', 'the-gioi-ngam-cua-ripley',
           'Tiểu thuyết tâm lý - trinh thám của Patricia Highsmith.'
    UNION ALL SELECT 'Trường Ca Achilles', 'truong-ca-achilles',
           'Tiểu thuyết của Madeline Miller.'
    UNION ALL SELECT 'Tư Duy Ngược Dịch Chuyển Thế Giới', 'tu-duy-nguoc-dich-chuyen-the-gioi',
           'Sách về tư duy đổi mới của Adam Grant.'
    UNION ALL SELECT 'Tuổi Trẻ Đáng Giá Bao Nhiêu', 'tuoi-tre-dang-gia-bao-nhieu',
           'Sách kỹ năng sống của Rosie Nguyễn.'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM books b WHERE b.slug = seed.slug
);

-- 5) Book - Author
INSERT IGNORE INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM (
    SELECT 'bieu-tuong-that-truyen' slug, 'Dan Brown' author
    UNION ALL SELECT 'cay-chuoi-non-di-giay-xanh', 'Nguyễn Nhật Ánh'
    UNION ALL SELECT 'chim-co-do', 'Jo Nesbø'
    UNION ALL SELECT 'cho-toi-xin-mot-ve-di-tuoi-tho', 'Nguyễn Nhật Ánh'
    UNION ALL SELECT 'di-tim-le-song', 'Viktor E. Frankl'
    UNION ALL SELECT 'harry-potter-va-hon-da-phu-thuy', 'J.K. Rowling'
    UNION ALL SELECT 'mat-biec', 'Nguyễn Nhật Ánh'
    UNION ALL SELECT 'mat-ma-da-vinci', 'Dan Brown'
    UNION ALL SELECT 'mua-do', 'Chu Lai'
    UNION ALL SELECT 'muu-sat', 'Tác giả đang cập nhật'
    UNION ALL SELECT 'ngay-xua-co-mot-chuyen-tinh', 'Nguyễn Nhật Ánh'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc', 'Avinash K. Dixit'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc', 'Barry J. Nalebuff'
    UNION ALL SELECT 'nha-gia-kim', 'Paulo Coelho'
    UNION ALL SELECT 'phia-sau-nghi-can-x', 'Higashino Keigo'
    UNION ALL SELECT 'rung-na-uy', 'Haruki Murakami'
    UNION ALL SELECT 'sapiens-luoc-su-loai-nguoi', 'Yuval Noah Harari'
    UNION ALL SELECT 'sherlock-holmes-toan-tap', 'Arthur Conan Doyle'
    UNION ALL SELECT 'tay-du-ky', 'Ngô Thừa Ân'
    UNION ALL SELECT 'tet-o-lang-dia-nguc', 'Thảo Trang'
    UNION ALL SELECT 'thao-tung-tam-ly', 'Shannon Thomas'
    UNION ALL SELECT 'the-gioi-ngam-cua-ripley', 'Patricia Highsmith'
    UNION ALL SELECT 'truong-ca-achilles', 'Madeline Miller'
    UNION ALL SELECT 'tu-duy-nguoc-dich-chuyen-the-gioi', 'Adam Grant'
    UNION ALL SELECT 'tuoi-tre-dang-gia-bao-nhieu', 'Rosie Nguyễn'
) seed
JOIN books b ON b.slug = seed.slug
JOIN authors a ON a.name = seed.author;

-- 6) Book - Category
INSERT IGNORE INTO book_categories (book_id, category_id)
SELECT b.id, c.id
FROM (
    SELECT 'bieu-tuong-that-truyen' slug, 'Trinh thám' category_name
    UNION ALL SELECT 'cay-chuoi-non-di-giay-xanh', 'Văn học'
    UNION ALL SELECT 'chim-co-do', 'Trinh thám'
    UNION ALL SELECT 'cho-toi-xin-mot-ve-di-tuoi-tho', 'Văn học'
    UNION ALL SELECT 'di-tim-le-song', 'Tâm lý'
    UNION ALL SELECT 'di-tim-le-song', 'Kỹ năng sống'
    UNION ALL SELECT 'harry-potter-va-hon-da-phu-thuy', 'Văn học'
    UNION ALL SELECT 'mat-biec', 'Văn học'
    UNION ALL SELECT 'mat-ma-da-vinci', 'Trinh thám'
    UNION ALL SELECT 'mua-do', 'Văn học'
    UNION ALL SELECT 'mua-do', 'Lịch sử'
    UNION ALL SELECT 'muu-sat', 'Trinh thám'
    UNION ALL SELECT 'ngay-xua-co-mot-chuyen-tinh', 'Văn học'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc', 'Kinh tế'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc', 'Kỹ năng sống'
    UNION ALL SELECT 'nha-gia-kim', 'Văn học'
    UNION ALL SELECT 'nha-gia-kim', 'Kỹ năng sống'
    UNION ALL SELECT 'phia-sau-nghi-can-x', 'Trinh thám'
    UNION ALL SELECT 'rung-na-uy', 'Văn học'
    UNION ALL SELECT 'sapiens-luoc-su-loai-nguoi', 'Lịch sử'
    UNION ALL SELECT 'sherlock-holmes-toan-tap', 'Trinh thám'
    UNION ALL SELECT 'tay-du-ky', 'Văn học'
    UNION ALL SELECT 'tet-o-lang-dia-nguc', 'Trinh thám'
    UNION ALL SELECT 'tet-o-lang-dia-nguc', 'Văn học'
    UNION ALL SELECT 'thao-tung-tam-ly', 'Tâm lý'
    UNION ALL SELECT 'thao-tung-tam-ly', 'Kỹ năng sống'
    UNION ALL SELECT 'the-gioi-ngam-cua-ripley', 'Trinh thám'
    UNION ALL SELECT 'the-gioi-ngam-cua-ripley', 'Văn học'
    UNION ALL SELECT 'truong-ca-achilles', 'Văn học'
    UNION ALL SELECT 'tu-duy-nguoc-dich-chuyen-the-gioi', 'Kỹ năng sống'
    UNION ALL SELECT 'tuoi-tre-dang-gia-bao-nhieu', 'Kỹ năng sống'
) seed
JOIN books b ON b.slug = seed.slug
JOIN categories c ON c.name = seed.category_name;

-- 7) Ebook PDF editions
-- Hai file PDF trong ZIP chưa có ảnh bìa khớp tên rõ ràng:
-- cho-toi-xin-mot-ve-di-tuoi-tho, mua-do => cover_object_name = NULL.
INSERT INTO book_editions (
    book_id, format, sku_code, original_price, sale_price,
    stock, is_active, cover_object_name, file_object_name, duration
)
SELECT b.id, 'EBOOK_PDF', seed.sku, seed.original_price, seed.sale_price,
       NULL, 1, seed.cover_path, seed.pdf_path, NULL
FROM books b
JOIN (
    SELECT 'bieu-tuong-that-truyen' slug, 'PDF-BTTT-001' sku, 79000 original_price, 49000 sale_price,
           'sach/anhbia/bieu-tuong-that-truyen.jpg' cover_path,
           'sach/_bieu-tuong-that-truyen.pdf' pdf_path
    UNION ALL SELECT 'cay-chuoi-non-di-giay-xanh','PDF-CCNDGX-001',69000,39000,
           'sach/anhbia/cay-chuoi-non-di-giay-xanh.jpg','sach/_cay-chuoi-non-di-giay-xanh.pdf'
    UNION ALL SELECT 'chim-co-do','PDF-CCD-001',79000,49000,
           'sach/anhbia/chim-co-do.jpg','sach/_chim-co-do.pdf'
    UNION ALL SELECT 'cho-toi-xin-mot-ve-di-tuoi-tho','PDF-CTXMVDTT-001',69000,39000,
           NULL,'sach/_cho-toi-xin-mot-ve-di-tuoi-tho.pdf'
    UNION ALL SELECT 'di-tim-le-song','PDF-DTLS-001',79000,49000,
           'sach/anhbia/di-tim-le-song.png','sach/_di-tim-le-song.pdf'
    UNION ALL SELECT 'harry-potter-va-hon-da-phu-thuy','PDF-HPHDP-001',89000,59000,
           'sach/anhbia/harry-potter-va-hon-da-phu-thuy.png','sach/_harry-potter-va-hon-da-phu-thuy.pdf'
    UNION ALL SELECT 'mat-biec','PDF-MB-001',69000,39000,
           'sach/anhbia/mat-biec.png','sach/_mat-biec.pdf'
    UNION ALL SELECT 'mat-ma-da-vinci','PDF-MMDV-001',89000,59000,
           'sach/anhbia/mat-ma-da-vinci.jpg','sach/_mat-ma-da-vinci.pdf'
    UNION ALL SELECT 'mua-do','PDF-MD-001',79000,49000,
           NULL,'sach/_mua-do.pdf'
    UNION ALL SELECT 'muu-sat','PDF-MS-001',79000,49000,
           'sach/anhbia/muu-sat.jpg','sach/_muu-sat.pdf'
    UNION ALL SELECT 'ngay-xua-co-mot-chuyen-tinh','PDF-NXCMT-001',69000,39000,
           'sach/anhbia/ngay-xua-co-mot-chuyen-tinh.jpg','sach/_ngay-xua-co-mot-chuyen-tinh.pdf'
    UNION ALL SELECT 'nghe-thuat-tu-duy-chien-luoc','PDF-NTTDCL-001',89000,59000,
           'sach/anhbia/nghe-thuat-tu-duy-chien-luoc.jpg','sach/_nghe-thuat-tu-duy-chien-luoc.pdf'
    UNION ALL SELECT 'nha-gia-kim','PDF-NGK-001',69000,39000,
           'sach/anhbia/nha-gia-kim.png','sach/_nha-gia-kim.pdf'
    UNION ALL SELECT 'phia-sau-nghi-can-x','PDF-PSNCX-001',79000,49000,
           'sach/anhbia/phia-sau-nghi-can-x.jpg','sach/_phia-sau-nghi-can-x.pdf'
    UNION ALL SELECT 'rung-na-uy','PDF-RNU-001',79000,49000,
           'sach/anhbia/rung-na-uy.png','sach/_rung-na-uy.pdf'
    UNION ALL SELECT 'sapiens-luoc-su-loai-nguoi','PDF-SAPIENS-001',99000,69000,
           'sach/anhbia/sapiens-luoc-su-loai-nguoi.png','sach/_sapiens-luoc-su-loai-nguoi.pdf'
    UNION ALL SELECT 'sherlock-holmes-toan-tap','PDF-SHTT-001',99000,69000,
           'sach/anhbia/sherlock-holmes-toan-tap.jpg','sach/_sherlock-holmes-toan-tap.pdf'
    UNION ALL SELECT 'tay-du-ky','PDF-TDK-001',89000,59000,
           'sach/anhbia/tay-du-ky.png','sach/_tay-du-ky.pdf'
    UNION ALL SELECT 'tet-o-lang-dia-nguc','PDF-TOLDN-001',79000,49000,
           'sach/anhbia/tet-o-lang-dia-nguc.jpg','sach/_tet-o-lang-dia-nguc.pdf'
    UNION ALL SELECT 'thao-tung-tam-ly','PDF-TTTL-001',79000,49000,
           'sach/anhbia/thao-tung-tam-ly.jpg','sach/_thao-tung-tam-ly.pdf'
    UNION ALL SELECT 'the-gioi-ngam-cua-ripley','PDF-TGNCR-001',89000,59000,
           'sach/anhbia/the-gioi-ngam-cua-ripley.jpg','sach/_the-gioi-ngam-cua-ripley.pdf'
    UNION ALL SELECT 'truong-ca-achilles','PDF-TCA-001',89000,59000,
           'sach/anhbia/truong-ca-achilles.jpg','sach/_truong-ca-achilles.pdf'
    UNION ALL SELECT 'tu-duy-nguoc-dich-chuyen-the-gioi','PDF-TDNDCTG-001',89000,59000,
           'sach/anhbia/tu-duy-nguoc-dich-chuyen-the-gioi.jpg','sach/_tu-duy-nguoc-dich-chuyen-the-gioi.pdf'
    UNION ALL SELECT 'tuoi-tre-dang-gia-bao-nhieu','PDF-TTDGBN-001',69000,39000,
           'sach/anhbia/tuoi-tre-dang-gia-bao-nhieu.png','sach/_tuoi-tre-dang-gia-bao-nhieu.pdf'
) seed ON seed.slug = b.slug
LEFT JOIN book_editions e ON e.sku_code = seed.sku
WHERE e.id IS NULL;

COMMIT;

-- Kiểm tra
SELECT
    b.id,
    b.title,
    b.slug,
    e.sku_code,
    e.sale_price,
    e.cover_object_name,
    e.file_object_name
FROM books b
JOIN book_editions e ON e.book_id = b.id
WHERE e.sku_code LIKE 'PDF-%-001'
ORDER BY b.id;
