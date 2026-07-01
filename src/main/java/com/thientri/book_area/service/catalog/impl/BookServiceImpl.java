package com.thientri.book_area.service.catalog.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.thientri.book_area.dto.request.catalog.BookCreateRequest;
import com.thientri.book_area.dto.request.catalog.BookUpdateRequest;
import com.thientri.book_area.dto.response.catalog.BookDetailResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.mapper.CatalogMapper;
import com.thientri.book_area.model.catalog.Author;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookImage;
import com.thientri.book_area.model.catalog.Category;
import com.thientri.book_area.model.catalog.Publisher;
import com.thientri.book_area.repository.catalog.AuthorRepository;
import com.thientri.book_area.repository.catalog.BookImageRepository;
import com.thientri.book_area.repository.catalog.BookRepository;
import com.thientri.book_area.repository.catalog.CategoryRepository;
import com.thientri.book_area.repository.catalog.PublisherRepository;
import com.thientri.book_area.service.catalog.IBookService;
import com.thientri.book_area.service.minio.MinioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements IBookService {

    // Tiêm các Repository và Service cần thiết
    private final BookRepository bookRepository;
    private final BookImageRepository bookImageRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    
    private final CatalogMapper catalogMapper;
    private final MinioService minioService;

    // ==========================================
    // NGHIỆP VỤ ĐỌC (FRONTEND HIỂN THỊ)
    // ==========================================
    
    @Override
    public BookDetailResponse getBookDetailBySlug(String slug) {
        Book book = bookRepository.findBySlug(slug)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy sách với đường dẫn: " + slug));

        // Tùy chọn: Chặn người dùng xem sách đã bị vô hiệu hóa (Chỉ Admin mới được xem)
        if (!book.getIsActive()) {
            throw new BadRequestException("Sách này hiện đã ngừng kinh doanh.");
        }

        // Tái sử dụng Mapper để làm phẳng dữ liệu
        return catalogMapper.toBookDetailResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDetailResponse> getAllBooks(Pageable pageable) {
        // Tối ưu hóa: Phân trang trực tiếp từ Database, map trực tiếp từ Entity sang DTO
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return bookPage.map(catalogMapper::toBookDetailResponse);
    }

    // ==========================================
    // NGHIỆP VỤ GHI (ADMIN QUẢN LÝ)
    // ==========================================

    @Override
    @Transactional // Đảm bảo tính toàn vẹn: Lỗi giữa chừng thì rollback toàn bộ DB
    public void createBook(BookCreateRequest request, List<MultipartFile> imageFiles) {
        
        // 1. Kiểm tra tính duy nhất của SEO Slug
        if (bookRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Đường dẫn (slug) này đã tồn tại. Vui lòng chọn tên khác.");
        }

        // 2. Lấy Nhà xuất bản
        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy nhà xuất bản với ID: " + request.getPublisherId()));

        // 3. TỐI ƯU HÓA HIỆU NĂNG: Lấy toàn bộ Tác giả và Danh mục bằng 1 câu lệnh SQL duy nhất (Batch Fetching)
        List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
        if (authors.size() != request.getAuthorIds().size()) {
            throw new BadRequestException("Một hoặc nhiều ID tác giả không hợp lệ.");
        }

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        if (categories.size() != request.getCategoryIds().size()) {
            throw new BadRequestException("Một hoặc nhiều ID danh mục không hợp lệ.");
        }

        // 4. Khởi tạo Sách gốc
        Book newBook = Book.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .publisher(publisher)
                .authors(new HashSet<>(authors))
                .categories(new HashSet<>(categories))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // 5. Xử lý Upload Ảnh và kết nối quan hệ 1-N (Book - BookImage)
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<BookImage> bookImages = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                // Upload lên bucket/folder 'books'
                String imageUrl = minioService.uploadFile(file, "books");
                
                BookImage bookImage = BookImage.builder()
                        .book(newBook) // Set ngược lại Book để Hibernate lưu khóa ngoại
                        .imageFileName(imageUrl)
                        .build();
                        
                bookImages.add(bookImage);
            }
            newBook.setImages(bookImages);
        }

        // 6. Lưu tất cả vào Database
        // Nhờ cấu hình CascadeType.ALL ở mảng Images, bạn chỉ cần save(newBook) là các ảnh tự động được lưu.
        bookRepository.save(newBook);
        log.info("Đã tạo thành công sách mới: {}", newBook.getTitle());
    }

    // Đổi trạng thái sách
    @Override
    @Transactional
    public void toggleBookActiveStatus(Long bookId, boolean isActive) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy sách với ID: " + bookId));
                
        book.setIsActive(isActive);
        bookRepository.save(book);
        log.info("Đã thay đổi trạng thái sách ID {} thành: {}", bookId, isActive ? "HOẠT ĐỘNG" : "VÔ HIỆU HÓA");
    }

    @Override
    @Transactional
    public void updateBook(Long bookId, BookUpdateRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy sách."));

        // Cập nhật thông tin text nếu có truyền lên
        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getSlug() != null && !request.getSlug().equals(book.getSlug())) {
            if (bookRepository.existsBySlug(request.getSlug())) {
                throw new BadRequestException("Slug đã tồn tại.");
            }
            book.setSlug(request.getSlug());
        }
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getIsActive() != null) book.setIsActive(request.getIsActive());

        // Cập nhật quan hệ 1-N (Tác giả, Danh mục)
        if (request.getAuthorIds() != null) {
            book.setAuthors(new HashSet<>(authorRepository.findAllById(request.getAuthorIds())));
        }
        if (request.getCategoryIds() != null) {
            book.setCategories(new HashSet<>(categoryRepository.findAllById(request.getCategoryIds())));
        }

        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void addBookImages(Long bookId, List<MultipartFile> imageFiles) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy sách."));
        
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = minioService.uploadFile(file, "books");
                bookImageRepository.save(BookImage.builder()
                        .book(book)
                        .imageFileName(imageUrl)
                        .build());
            }
        }
    }

    @Override
    @Transactional
    public void deleteBookImage(Long imageId) {
        BookImage image = bookImageRepository.findById(imageId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy ảnh."));
        
        // 1. Xóa file vật lý trên MinIO trước
        if (image.getImageFileName() != null) {
            minioService.deleteFile(image.getImageFileName());
        }
        
        // 2. Xóa record trong Database
        bookImageRepository.delete(image);
    }
}