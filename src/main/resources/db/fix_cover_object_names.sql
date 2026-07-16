-- Đồng bộ object key trong SQL Server với các file hiện có trong bucket
-- book-area-files/sach/anhbia trên MinIO. Những sách chưa có file bìa
-- tương ứng được đặt NULL để giao diện dùng ảnh thay thế, thay vì gọi URL 404.
USE book_area3;
GO

UPDATE book_editions
SET cover_object_name = CASE
    WHEN id IN (1, 7) THEN N'sach/anhbia/mat-biec.png'
    WHEN id = 2 THEN N'sach/anhbia/rung-na-uy.png'
    WHEN id IN (3, 8) THEN N'sach/anhbia/harry-potter-va-hon-da-phu-thuy.png'
    WHEN id = 9 THEN N'sach/anhbia/bieu-tuong-that-truyen.jpg'
    WHEN id = 10 THEN N'sach/anhbia/cay-chuoi-non-di-giay-xanh.jpg'
    WHEN id = 11 THEN N'sach/anhbia/chim-co-do.jpg'
    WHEN id = 14 THEN N'sach/anhbia/mat-ma-da-vinci.jpg'
    WHEN id = 16 THEN N'sach/anhbia/muu-sat.jpg'
    WHEN id = 17 THEN N'sach/anhbia/ngay-xua-co-mot-chuyen-tinh.jpg'
    WHEN id = 18 THEN N'sach/anhbia/nghe-thuat-tu-duy-chien-luoc.jpg'
    WHEN id = 19 THEN N'sach/anhbia/nha-gia-kim.png'
    WHEN id = 20 THEN N'sach/anhbia/phia-sau-nghi-can-x.jpg'
    WHEN id = 22 THEN N'sach/anhbia/sherlock-holmes-toan-tap.jpg'
    WHEN id = 24 THEN N'sach/anhbia/tet-o-lang-dia-nguc.jpg'
    WHEN id = 25 THEN N'sach/anhbia/thao-tung-tam-ly.jpg'
    WHEN id = 26 THEN N'sach/anhbia/the-gioi-ngam-cua-ripley.jpg'
    WHEN id = 27 THEN N'sach/anhbia/truong-ca-achilles.jpg'
    WHEN id = 28 THEN N'sach/anhbia/tu-duy-nguoc-dich-chuyen-the-gioi.jpg'
    WHEN id = 29 THEN N'sach/anhbia/tuoi-tre-dang-gia-bao-nhieu.png'
    WHEN id IN (12, 13, 15, 21, 23) THEN NULL
    ELSE cover_object_name
END
WHERE id IN (1, 2, 3, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
             22, 23, 24, 25, 26, 27, 28, 29);
GO
