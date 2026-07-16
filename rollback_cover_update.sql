-- Rollback cover_object_name updates
-- Generated: 2026-07-02T15:10:06.465060300
BEGIN TRANSACTION;

UPDATE book_editions SET cover_object_name = 'sach/anhbia/mat-biec.png' WHERE id = 1; -- Mắt Biếc
UPDATE book_editions SET cover_object_name = 'sach/anhbia/rung-na-uy.png' WHERE id = 2; -- Rừng Na Uy
UPDATE book_editions SET cover_object_name = 'sach/anhbia/bieu-tuong-that-truyen.jpg' WHERE id = 7; -- Biểu Tượng Thất Truyền
UPDATE book_editions SET cover_object_name = 'sach/anhbia/cay-chuoi-non-di-giay-xanh.jpg' WHERE id = 8; -- Cây Chuối Non Đi Giày Xanh
UPDATE book_editions SET cover_object_name = 'sach/anhbia/chim-co-do.jpg' WHERE id = 9; -- Chim Cổ Đỏ
UPDATE book_editions SET cover_object_name = 'sach/anhbia/cho-toi-xin-mot-ve-di-tuoi-tho.jpg' WHERE id = 10; -- Cho Tôi Xin Một Vé Đi Tuổi Thơ
UPDATE book_editions SET cover_object_name = 'sach/anhbia/di-tim-le-song.jpg' WHERE id = 11; -- Đi Tìm Lẽ Sống
UPDATE book_editions SET cover_object_name = 'sach/anhbia/mat-ma-da-vinci.jpg' WHERE id = 12; -- Mật Mã Da Vinci
UPDATE book_editions SET cover_object_name = 'sach/anhbia/mua-do.png' WHERE id = 13; -- Mưa Đỏ
UPDATE book_editions SET cover_object_name = 'sach/anhbia/muu-sat.jpg' WHERE id = 14; -- Mưu Sát
UPDATE book_editions SET cover_object_name = 'sach/anhbia/ngay-xua-co-mot-chuyen-tinh.jpg' WHERE id = 15; -- Ngày Xưa Có Một Chuyện Tình
UPDATE book_editions SET cover_object_name = 'sach/anhbia/nghe-thuat-tu-duy-chien-luoc.jpg' WHERE id = 16; -- Nghệ Thuật Tư Duy Chiến Lược
UPDATE book_editions SET cover_object_name = 'sach/anhbia/nha-gia-kim.png' WHERE id = 17; -- Nhà Giả Kim
UPDATE book_editions SET cover_object_name = 'sach/anhbia/phia-sau-nghi-can-x.jpg' WHERE id = 18; -- Phía Sau Nghi Can X
UPDATE book_editions SET cover_object_name = 'sach/anhbia/sapiens-luoc-su-loai-nguoi.jpg' WHERE id = 19; -- Sapiens Lược Sử Loài Người
UPDATE book_editions SET cover_object_name = 'sach/anhbia/sherlock-holmes-toan-tap.jpg' WHERE id = 20; -- Sherlock Holmes Toàn Tập
UPDATE book_editions SET cover_object_name = 'sach/anhbia/tay-du-ky.png' WHERE id = 21; -- Tây Du Ký
UPDATE book_editions SET cover_object_name = 'sach/anhbia/tet-o-lang-dia-nguc.jpg' WHERE id = 22; -- Tết Ở Làng Địa Ngục
UPDATE book_editions SET cover_object_name = 'sach/anhbia/thao-tung-tam-ly.jpg' WHERE id = 23; -- Thao Túng Tâm Lý
UPDATE book_editions SET cover_object_name = 'sach/anhbia/the-gioi-ngam-cua-ripley.jpg' WHERE id = 24; -- Thế Giới Ngầm Của Ripley
UPDATE book_editions SET cover_object_name = 'sach/anhbia/truong-ca-achilles.jpg' WHERE id = 25; -- Trường Ca Achilles
UPDATE book_editions SET cover_object_name = 'sach/anhbia/tu-duy-nguoc-dich-chuyen-the-gioi.jpg' WHERE id = 26; -- Tư Duy Ngược Dịch Chuyển Thế Giới
UPDATE book_editions SET cover_object_name = 'sach/anhbia/tuoi-tre-dang-gia-bao-nhieu.png' WHERE id = 27; -- Tuổi Trẻ Đáng Giá Bao Nhiêu

COMMIT;
