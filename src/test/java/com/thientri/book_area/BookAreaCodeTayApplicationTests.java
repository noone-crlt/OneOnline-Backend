package com.thientri.book_area;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import io.minio.MinioClient;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import com.thientri.book_area.service.minio.MinioService;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.math.BigDecimal;
import java.util.List;
import com.thientri.book_area.repository.catalog.BookRepository;
import com.thientri.book_area.repository.catalog.BookEditionRepository;
import com.thientri.book_area.repository.catalog.AuthorRepository;
import com.thientri.book_area.repository.catalog.PublisherRepository;
import com.thientri.book_area.repository.catalog.CategoryRepository;
import com.thientri.book_area.model.catalog.Author;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookEdition;

@SpringBootTest
class BookAreaCodeTayApplicationTests {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioService minioService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookEditionRepository bookEditionRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void cleanupDuplicateCovers() {
        try {
            System.out.println("========== CLEANUP DUPLICATE / UNUSED COVERS ==========");

            // 1. Collect all cover_object_name values actually used in DB
            java.util.Set<String> usedCovers = new java.util.HashSet<>();
            for (BookEdition ed : bookEditionRepository.findAll()) {
                if (ed.getCoverObjectName() != null && !ed.getCoverObjectName().isBlank()) {
                    usedCovers.add(ed.getCoverObjectName());
                }
            }
            System.out.println("\nCovers referenced in DB (" + usedCovers.size() + "):");
            usedCovers.stream().sorted().forEach(c -> System.out.println("  [USED] " + c));

            // 2. List all cover files in local anhbia directory
            File anhbiaDir = new File("d:\\book-area-code-tay\\sach\\anhbia");
            File[] localFiles = anhbiaDir.listFiles();
            java.util.List<File> filesToDeleteLocal = new java.util.ArrayList<>();
            if (localFiles != null) {
                System.out.println("\nLocal files in anhbia (" + localFiles.length + "):");
                for (File f : localFiles) {
                    String objectName = "sach/anhbia/" + f.getName();
                    boolean isUsed = usedCovers.contains(objectName);
                    System.out.println("  " + (isUsed ? "[KEEP]" : "[DELETE]") + " " + f.getName() + " (" + f.length() + " bytes)");
                    if (!isUsed) {
                        filesToDeleteLocal.add(f);
                    }
                }
            }

            // 3. List all cover objects on MinIO and find unused ones
            String bucket = "book-area-files";
            java.util.List<String> minioObjectsToDelete = new java.util.ArrayList<>();
            System.out.println("\nMinIO objects under sach/anhbia/:");
            for (Result<Item> result : minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).prefix("sach/anhbia/").build())) {
                Item item = result.get();
                String objectName = item.objectName();
                boolean isUsed = usedCovers.contains(objectName);
                System.out.println("  " + (isUsed ? "[KEEP]" : "[DELETE]") + " " + objectName + " (" + item.size() + " bytes)");
                if (!isUsed) {
                    minioObjectsToDelete.add(objectName);
                }
            }

            // 4. Delete unused local files
            System.out.println("\n--- Deleting " + filesToDeleteLocal.size() + " unused local files ---");
            for (File f : filesToDeleteLocal) {
                boolean deleted = f.delete();
                System.out.println("  " + (deleted ? "DELETED" : "FAILED") + " local: " + f.getName());
            }

            // 5. Delete unused MinIO objects
            System.out.println("\n--- Deleting " + minioObjectsToDelete.size() + " unused MinIO objects ---");
            for (String objectName : minioObjectsToDelete) {
                minioClient.removeObject(io.minio.RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
                System.out.println("  DELETED minio: " + objectName);
            }

            System.out.println("\n========== CLEANUP COMPLETE ==========");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.test.annotation.Rollback(false)
    void updateDbCoverExtensions() {
        try {
            System.out.println("========== UPDATING DB COVER EXTENSIONS ==========");
            
            java.util.Map<String, String> mappings = java.util.Map.of(
                "mat-biec", "sach/anhbia/mat-biec.png",
                "rung-na-uy", "sach/anhbia/rung-na-uy.png",
                "harry-potter-1", "sach/anhbia/harry-potter-va-hon-da-phu-thuy.png",
                "nha-gia-kim", "sach/anhbia/nha-gia-kim.png",
                "tuoi-tre-dang-gia-bao-nhieu", "sach/anhbia/tuoi-tre-dang-gia-bao-nhieu.png",
                "di-tim-le-song", "sach/anhbia/di-tim-le-song.png",
                "sapiens-luoc-su-loai-nguoi", "sach/anhbia/sapiens-luoc-su-loai-nguoi.png"
            );

            for (var entry : mappings.entrySet()) {
                bookRepository.findBySlug(entry.getKey()).ifPresent(book -> {
                    for (BookEdition ed : book.getEditions()) {
                        ed.setCoverObjectName(entry.getValue());
                        bookEditionRepository.save(ed);
                    }
                    System.out.println("Updated " + entry.getKey() + " -> " + entry.getValue());
                });
            }
            System.out.println("==================================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.test.annotation.Rollback(false)
    void seedDatabaseWithAllBooks() {
        try {
            System.out.println("========== SEEDING DATABASE WITH ALL BOOKS ==========");
            
            // Get default publisher and category
            var publishers = publisherRepository.findAll();
            var publisher = publishers.isEmpty() ? null : publishers.get(0);
            
            var categories = categoryRepository.findAll();
            var category = categories.isEmpty() ? null : categories.get(0);

            // 1. Helper to find/create author
            java.util.function.Function<String, Author> findOrCreateAuthor = (name) -> {
                return authorRepository.findByName(name).orElseGet(() -> {
                    Author newAuthor = Author.builder().name(name).build();
                    return authorRepository.save(newAuthor);
                });
            };

            // 2. Update existing books & editions
            // Book 1: Mắt Biếc
            bookRepository.findBySlug("mat-biec").ifPresent(book -> {
                for (BookEdition ed : book.getEditions()) {
                    if ("PHYSICAL".equals(ed.getFormat())) {
                        ed.setCoverObjectName("sach/anhbia/mat-biec.jpg");
                        bookEditionRepository.save(ed);
                    }
                }
            });

            // Book 2: Rừng Na Uy
            bookRepository.findBySlug("rung-na-uy").ifPresent(book -> {
                for (BookEdition ed : book.getEditions()) {
                    if ("EBOOK_PDF".equals(ed.getFormat())) {
                        ed.setCoverObjectName("sach/anhbia/rung-na-uy.jpg");
                        ed.setFileObjectName("sach/_rung-na-uy.pdf");
                        bookEditionRepository.save(ed);
                    }
                }
            });

            // Book 3: Harry Potter và Hòn đá Phù thủy
            bookRepository.findBySlug("harry-potter-1").ifPresent(book -> {
                // Update audiobook cover
                for (BookEdition ed : book.getEditions()) {
                    if ("AUDIOBOOK".equals(ed.getFormat())) {
                        ed.setCoverObjectName("sach/anhbia/harry-potter-va-hon-da-phu-thuy.jpg");
                        bookEditionRepository.save(ed);
                    }
                }
                // Check if PDF edition already exists, if not create it
                boolean hasPdf = book.getEditions().stream().anyMatch(e -> "EBOOK_PDF".equals(e.getFormat()));
                if (!hasPdf) {
                    BookEdition pdfEd = BookEdition.builder()
                            .book(book)
                            .format("EBOOK_PDF")
                            .skuCode("PDF-HP1-001")
                            .originalPrice(new BigDecimal("50000"))
                            .salePrice(new BigDecimal("30000"))
                            .coverObjectName("sach/anhbia/harry-potter-va-hon-da-phu-thuy.jpg")
                            .fileObjectName("sach/_harry-potter-va-hon-da-phu-thuy.pdf")
                            .isActive(true)
                            .build();
                    bookEditionRepository.save(pdfEd);
                }
            });

            // 3. Define and insert all other 19 books
            class BookData {
                String title;
                String slug;
                String authorName;
                String coverPath;
                String pdfPath;

                BookData(String title, String slug, String authorName, String coverPath, String pdfPath) {
                    this.title = title;
                    this.slug = slug;
                    this.authorName = authorName;
                    this.coverPath = coverPath;
                    this.pdfPath = pdfPath;
                }
            }

            List<BookData> newBooks = List.of(
                new BookData("Biểu Tượng Thất Truyền", "bieu-tuong-that-truyen", "Dan Brown", "sach/anhbia/bieu-tuong-that-truyen.jpg", "sach/_bieu-tuong-that-truyen.pdf"),
                new BookData("Cây Chuối Non Đi Giày Xanh", "cay-chuoi-non-di-giay-xanh", "Nguyễn Nhật Ánh", "sach/anhbia/cay-chuoi-non-di-giay-xanh.jpg", "sach/_cay-chuoi-non-di-giay-xanh.pdf"),
                new BookData("Chim Cổ Đỏ", "chim-co-do", "Jo Nesbo", "sach/anhbia/chim-co-do.jpg", "sach/_chim-co-do.pdf"),
                new BookData("Cho Tôi Xin Một Vé Đi Tuổi Thơ", "cho-toi-xin-mot-ve-di-tuoi-tho", "Nguyễn Nhật Ánh", "sach/anhbia/cho-toi-xin-mot-ve-di-tuoi-tho.jpg", "sach/_cho-toi-xin-mot-ve-di-tuoi-tho.pdf"),
                new BookData("Đi Tìm Lẽ Sống", "di-tim-le-song", "Viktor Frankl", "sach/anhbia/di-tim-le-song.png", "sach/_di-tim-le-song.pdf"),
                new BookData("Mật Mã Da Vinci", "mat-ma-da-vinci", "Dan Brown", "sach/anhbia/mat-ma-da-vinci.jpg", "sach/_mat-ma-da-vinci.pdf"),
                new BookData("Mưa Đỏ", "mua-do", "Chu Lai", "sach/anhbia/mua-do.jpg", "sach/_mua-do.pdf"),
                new BookData("Mưu Sát", "muu-sat", "Tử Kim Trần", "sach/anhbia/muu-sat.jpg", "sach/_muu-sat.pdf"),
                new BookData("Ngày Xưa Có Một Chuyện Tình", "ngay-xua-co-mot-chuyen-tinh", "Nguyễn Nhật Ánh", "sach/anhbia/ngay-xua-co-mot-chuyen-tinh.jpg", "sach/_ngay-xua-co-mot-chuyen-tinh.pdf"),
                new BookData("Nghệ Thuật Tư Duy Chiến Lược", "nghe-thuat-tu-duy-chien-luoc", "Barry J. Nalebuff", "sach/anhbia/nghe-thuat-tu-duy-chien-luoc.jpg", "sach/_nghe-thuat-tu-duy-chien-luoc.pdf"),
                new BookData("Nhà Giả Kim", "nha-gia-kim", "Paulo Coelho", "sach/anhbia/nha-gia-kim.jpg", "sach/_nha-gia-kim.pdf"),
                new BookData("Phía Sau Nghi Can X", "phia-sau-nghi-can-x", "Higashino Keigo", "sach/anhbia/phia-sau-nghi-can-x.jpg", "sach/_phia-sau-nghi-can-x.pdf"),
                new BookData("Sapiens Lược Sử Loài Người", "sapiens-luoc-su-loai-nguoi", "Yuval Noah Harari", "sach/anhbia/sapiens-luoc-su-loai-nguoi.png", "sach/_sapiens-luoc-su-loai-nguoi.pdf"),
                new BookData("Sherlock Holmes Toàn Tập", "sherlock-holmes-toan-tap", "Arthur Conan Doyle", "sach/anhbia/sherlock-holmes-toan-tap.jpg", "sach/_sherlock-holmes-toan-tap.pdf"),
                new BookData("Tây Du Ký", "tay-du-ky", "Ngô Thừa Ân", "sach/anhbia/tay-du-ky.jpg", "sach/_tay-du-ky.pdf"),
                new BookData("Tết Ở Làng Địa Ngục", "tet-o-lang-dia-nguc", "Thảo Trang", "sach/anhbia/tet-o-lang-dia-nguc.jpg", "sach/_tet-o-lang-dia-nguc.pdf"),
                new BookData("Thao Túng Tâm Lý", "thao-tung-tam-ly", "Shannon Thomas", "sach/anhbia/thao-tung-tam-ly.jpg", "sach/_thao-tung-tam-ly.pdf"),
                new BookData("Thế Giới Ngầm Của Ripley", "the-gioi-ngam-cua-ripley", "Patricia Highsmith", "sach/anhbia/the-gioi-ngam-cua-ripley.jpg", "sach/_the-gioi-ngam-cua-ripley.pdf"),
                new BookData("Trường Ca Achilles", "truong-ca-achilles", "Madeline Miller", "sach/anhbia/truong-ca-achilles.jpg", "sach/_truong-ca-achilles.pdf"),
                new BookData("Tư Duy Ngược Dịch Chuyển Thế Giới", "tu-duy-nguoc-dich-chuyen-the-gioi", "Adam Grant", "sach/anhbia/tu-duy-nguoc-dich-chuyen-the-gioi.jpg", "sach/_tu-duy-nguoc-dich-chuyen-the-gioi.pdf"),
                new BookData("Tuổi Trẻ Đáng Giá Bao Nhiêu", "tuoi-tre-dang-gia-bao-nhieu", "Rosie Nguyễn", "sach/anhbia/tuoi-tre-dang-gia-bao-nhieu.jpg", "sach/_tuoi-tre-dang-gia-bao-nhieu.pdf")
            );

            for (BookData data : newBooks) {
                if (bookRepository.findBySlug(data.slug).isPresent()) {
                    System.out.println("Book with slug " + data.slug + " already exists. Skipping.");
                    continue;
                }

                // 1. Create or get author
                Author author = findOrCreateAuthor.apply(data.authorName);

                // 2. Create Book
                Book newBook = Book.builder()
                        .title(data.title)
                        .slug(data.slug)
                        .description(data.title + " - một tác phẩm xuất sắc của " + data.authorName)
                        .publisher(publisher)
                        .isActive(true)
                        .build();
                
                if (category != null) {
                    newBook.getCategories().add(category);
                }
                newBook.getAuthors().add(author);
                
                Book savedBook = bookRepository.save(newBook);
                System.out.println("Created book: " + savedBook.getTitle());

                // 3. Create BookEdition (EBOOK_PDF)
                BookEdition pdfEd = BookEdition.builder()
                        .book(savedBook)
                        .format("EBOOK_PDF")
                        .skuCode("PDF-" + data.slug.toUpperCase() + "-001")
                        .originalPrice(new BigDecimal("50000"))
                        .salePrice(new BigDecimal("30000"))
                        .coverObjectName(data.coverPath)
                        .fileObjectName(data.pdfPath)
                        .isActive(true)
                        .build();
                
                bookEditionRepository.save(pdfEd);
                System.out.println("Created PDF edition for: " + savedBook.getTitle());
            }

            System.out.println("=====================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void listMinioObjects() {
        try {
            System.out.println("========== MINIO OBJECTS LIST ==========");
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket("book-area-files")
                            .recursive(true)
                            .build());
            for (Result<Item> result : results) {
                Item item = result.get();
                System.out.println("Object: " + item.objectName() + " | Size: " + item.size() + " bytes");
            }
            System.out.println("========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void uploadAllLocalFilesToMinio() {
        try {
            System.out.println("========== UPLOADING LOCAL FILES TO MINIO ==========");
            File sachFolder = new File("d:\\book-area-code-tay\\sach");
            File anhbiaFolder = new File("d:\\book-area-code-tay\\sach\\anhbia");

            // 1. Upload PDFs
            File[] pdfFiles = sachFolder.listFiles((dir, name) -> name.startsWith("_") && name.endsWith(".pdf"));
            if (pdfFiles != null) {
                for (File file : pdfFiles) {
                    String objectName = "sach/" + file.getName();
                    String contentType = "application/pdf";
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                        minioClient.putObject(io.minio.PutObjectArgs.builder()
                                .bucket("book-area-files")
                                .object(objectName)
                                .stream(fis, file.length(), -1)
                                .contentType(contentType)
                                .build());
                        System.out.println("Uploaded PDF: " + objectName);
                    }
                }
            }

            // 2. Upload Covers
            File[] coverFiles = anhbiaFolder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".webp"));
            if (coverFiles != null) {
                for (File file : coverFiles) {
                    String objectName = "sach/anhbia/" + file.getName();
                    String contentType = file.getName().endsWith(".png") ? "image/png" : "image/jpeg";
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                        minioClient.putObject(io.minio.PutObjectArgs.builder()
                                .bucket("book-area-files")
                                .object(objectName)
                                .stream(fis, file.length(), -1)
                                .contentType(contentType)
                                .build());
                        System.out.println("Uploaded Cover: " + objectName);
                    }
                }
            }
            System.out.println("====================================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void fixDefaultCovers() {
        try {
            System.out.println("========== FIXING DEFAULT COVERS ==========");
            
            // 1. Hiểu về trái tim (cover4.jpg)
            String hieuVeTraiTimUrl = "https://picsum.photos/id/24/600/900";
            downloadAndUploadToMinio(hieuVeTraiTimUrl, "cover4.jpg");

            // 2. Số Đỏ (cover5.jpg)
            String soDoUrl = "https://picsum.photos/id/20/600/900";
            downloadAndUploadToMinio(soDoUrl, "cover5.jpg");

            System.out.println("===========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadAndUploadToMinio(String urlStr, String objectName) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlStr))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .GET()
                .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() == 200) {
            String contentType = response.headers().firstValue("Content-Type").orElse("image/jpeg");
            byte[] bytes = response.body().readAllBytes();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                minioService.uploadInputStream(bais, objectName, contentType, bytes.length);
                System.out.println("Uploaded " + objectName + " successfully to MinIO.");
            }
        } else {
            System.err.println("Failed to download image from " + urlStr + " | status: " + response.statusCode());
        }
    }
}
