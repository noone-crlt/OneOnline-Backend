package com.thientri.book_area.service.minio;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor // SỬA 1: Dùng Lombok thay cho @Autowired
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Value("${minio.endpoint}") // SỬA 2: Xóa dấu "/" thừa
    private String minioEndpoint;

    /**
     * Upload file lên MinIO có phân loại thư mục
     * @param file File cần upload
     * @param folder Tên thư mục (vd: "images", "pdfs", "audio")
     * @return Tên file (objectName) đã lưu trên MinIO
     */
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload không được để trống");
        }

        // SỬA 3: Chỉ trích xuất đuôi file (extension) để chống lỗi font chữ / ký tự đặc biệt
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Tạo tên file chuẩn: thư-mục/uuid.đuôi
        String uniqueFilename = folder + "/" + UUID.randomUUID().toString() + extension;
        String contentType = file.getContentType();
        long size = file.getSize();

        // SỬA 4: Dùng try-with-resources để TỰ ĐỘNG ĐÓNG LUỒNG InputStream chống tràn RAM
        try (InputStream inputStream = file.getInputStream()) {
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(uniqueFilename)
                    // Với sách nói/PDF nặng, partSize -1 để MinIO tự tính toán tối ưu dựa trên size
                    .stream(inputStream, size, -1) 
                    .contentType(contentType)
                    .build());

            log.info("Upload file thành công: {}", uniqueFilename);
            return uniqueFilename;
            
        } catch (Exception e) {
            log.error("Lỗi khi upload file lên MinIO: ", e);
            throw new RuntimeException("Không thể upload file: " + e.getMessage());
        }
    }

    // Lấy link có thời hạn (Dùng cho Sách PDF, EPUB, MP3 để chống tải lậu)
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(2, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            log.error("Lỗi khi tạo URL có thời hạn cho file {}: ", objectName, e);
            throw new RuntimeException("Không thể lấy dữ liệu file");
        }
    }

    // Lấy link Public (Chỉ dùng cho Ảnh bìa sách - Yêu cầu Bucket phải set quyền Public Read)
    public String getPublicImageUrl(String objectName) {
        // Đảm bảo không bị dư dấu slash nếu cấu hình bị lỗi
        String endpoint = minioEndpoint.endsWith("/") ? minioEndpoint.substring(0, minioEndpoint.length() - 1) : minioEndpoint;
        return endpoint + "/" + bucketName + "/" + objectName;
    }

    // Xóa file
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("Đã xóa file: {}", objectName);
        } catch (Exception e) {
            log.error("Lỗi khi xóa file {}: ", objectName, e);
            throw new RuntimeException("Không thể xóa file");
        }
    }

    /**
     * Upload InputStream lên MinIO có hỗ trợ luồng kích thước chưa rõ
     */
    public String uploadInputStream(InputStream inputStream, String objectName, String contentType, long size) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream không được để trống");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content-Type không được để trống");
        }

        // Validate MIME type
        String ext;
        String cleanContentType = contentType.trim().toLowerCase();
        if (cleanContentType.equals("image/jpeg") || cleanContentType.equals("image/jpg")) {
            ext = ".jpg";
            cleanContentType = "image/jpeg";
        } else if (cleanContentType.equals("image/png")) {
            ext = ".png";
        } else if (cleanContentType.equals("image/webp")) {
            ext = ".webp";
        } else {
            throw new IllegalArgumentException("Định dạng ảnh không được hỗ trợ: " + contentType);
        }

        // Generate object name if needed
        String finalObjectName = objectName;
        if (finalObjectName == null || finalObjectName.isBlank() || finalObjectName.endsWith("/")) {
            String folder = (finalObjectName != null && !finalObjectName.isBlank()) ? finalObjectName : "covers/google-books/";
            finalObjectName = folder + UUID.randomUUID().toString() + ext;
        }

        try {
            long putSize = size >= 0 ? size : -1;
            long partSize = size >= 0 ? -1 : 5L * 1024 * 1024; // 5MB part size for unknown size

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(finalObjectName)
                    .stream(inputStream, putSize, partSize)
                    .contentType(cleanContentType)
                    .build());

            log.info("Upload InputStream thành công: {}", finalObjectName);
            return finalObjectName;
        } catch (Exception e) {
            log.error("Lỗi khi upload InputStream lên MinIO: ", e);
            throw new RuntimeException("Không thể upload file từ stream: " + e.getMessage());
        }
    }
}