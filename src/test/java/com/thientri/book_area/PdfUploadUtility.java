package com.thientri.book_area;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookEdition;
import com.thientri.book_area.repository.catalog.BookEditionRepository;
import com.thientri.book_area.repository.catalog.BookRepository;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Optional;

@SpringBootTest
public class PdfUploadUtility {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookEditionRepository bookEditionRepository;

    @Value("${minio.bucket.name:book-area-files}")
    private String bucketName;

    private static final Path SACH_DIR = Path.of("d:/book-area-code-tay/sach");

    @Test
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.test.annotation.Rollback(false)
    public void uploadPdfsToMinioAndSyncDatabase() {
        System.out.println("========== BẮT ĐẦU TẢI SÁCH PDF LÊN MINIO ==========\n");

        File[] pdfFiles = SACH_DIR.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) {
            System.err.println("Không tìm thấy file PDF nào trong thư mục " + SACH_DIR);
            return;
        }

        int uploadCount = 0;
        int dbUpdateCount = 0;

        for (File file : pdfFiles) {
            String fileName = file.getName();
            String slug = fileName.replace("_.pdf", "").replace("_", "").replace(".pdf", "");
            String objectName = "ebooks/" + fileName;

            System.out.printf("Đang xử lý: %s (Slug: %s)%n", fileName, slug);

            // 1. Upload lên MinIO
            try {
                // Kiểm tra xem file đã tồn tại chưa
                boolean exists = true;
                try {
                    minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
                } catch (Exception e) {
                    exists = false;
                }

                if (!exists) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        minioClient.putObject(
                            PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(fis, file.length(), -1)
                                .contentType("application/pdf")
                                .build()
                        );
                        uploadCount++;
                        System.out.println("  -> Đã upload lên MinIO: " + objectName);
                    }
                } else {
                    System.out.println("  -> File đã tồn tại trên MinIO: " + objectName);
                }
            } catch (Exception e) {
                System.err.println("  -> LỖI Upload MinIO: " + e.getMessage());
                continue;
            }

            // 2. Cập nhật Database
            Optional<Book> bookOpt = bookRepository.findBySlug(slug);
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                
                // Kiểm tra xem đã có edition EBOOK_PDF chưa
                boolean hasPdfEdition = false;
                for (BookEdition edition : book.getEditions()) {
                    if ("EBOOK_PDF".equals(edition.getFormat())) {
                        hasPdfEdition = true;
                        if (edition.getFileObjectName() == null || !edition.getFileObjectName().equals(objectName)) {
                            edition.setFileObjectName(objectName);
                            bookEditionRepository.save(edition);
                            dbUpdateCount++;
                            System.out.println("  -> Đã cập nhật file_object_name cho edition hiện tại.");
                        }
                        break;
                    }
                }
                
                if (!hasPdfEdition) {
                    BookEdition newEdition = new BookEdition();
                    newEdition.setBook(book);
                    newEdition.setFormat("EBOOK_PDF");
                    newEdition.setSalePrice(java.math.BigDecimal.ZERO);
                    newEdition.setIsActive(true);
                    newEdition.setFileObjectName(objectName);
                    
                    // Lấy bìa từ physical format nếu có
                    for (BookEdition ed : book.getEditions()) {
                        if (ed.getCoverObjectName() != null) {
                            newEdition.setCoverObjectName(ed.getCoverObjectName());
                            break;
                        }
                    }
                    
                    bookEditionRepository.save(newEdition);
                    dbUpdateCount++;
                    System.out.println("  -> Đã tạo mới EBOOK_PDF edition trong CSDL.");
                }
            } else {
                System.out.println("  -> BỎ QUA CSDL: Không tìm thấy sách có slug = " + slug);
            }
        }

        System.out.printf("%n========== HOÀN TẤT ==========%n");
        System.out.printf("Số lượng file tải lên MinIO: %d%n", uploadCount);
        System.out.printf("Số lượng bản ghi CSDL cập nhật: %d%n", dbUpdateCount);
    }
}
