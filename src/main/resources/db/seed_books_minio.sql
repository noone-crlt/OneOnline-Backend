USE book_area1;
GO

UPDATE books
SET pdf_object_name = N'sach/_cay-chuoi-non-di-giay-xanh.pdf',
    cover_object_name = N'sach/anhbia/cay-chuoi-non-di-giay-xanh-pdf-epub-azw3-mobi.jpg'
WHERE id = 3;

UPDATE books
SET pdf_object_name = N'sach/_di-tim-le-song.pdf',
    cover_object_name = N'sach/anhbia/di-tim-le-song.png'
WHERE id = 8;

UPDATE books
SET pdf_object_name = N'sach/_harry-potter-va-hon-da-phu-thuy.pdf',
    cover_object_name = N'sach/anhbia/harrypotter.png'
WHERE id = 1;

UPDATE books
SET pdf_object_name = N'sach/_mat-biec.pdf',
    cover_object_name = N'sach/anhbia/matbiec.png'
WHERE id = 2;

UPDATE books
SET pdf_object_name = N'sach/_nha-gia-kim.pdf',
    cover_object_name = N'sach/anhbia/nhagiakim.png'
WHERE id = 4;

UPDATE books
SET pdf_object_name = N'sach/_rung-na-uy.pdf',
    cover_object_name = N'sach/anhbia/rungnauy.png'
WHERE id = 10;

UPDATE books
SET pdf_object_name = N'sach/_sapiens-luoc-su-loai-nguoi.pdf',
    cover_object_name = N'sach/anhbia/sapiens-luoc-su-loai-nguoi.png'
WHERE id = 7;

UPDATE books
SET pdf_object_name = N'sach/_tuoi-tre-dang-gia-bao-nhieu.pdf',
    cover_object_name = N'sach/anhbia/tui-tre-dang-gia-bao-nhieu.png'
WHERE id = 9;
GO
