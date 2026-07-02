package com.thientri.book_area;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import com.thientri.book_area.repository.catalog.BookRepository;
import com.thientri.book_area.repository.catalog.BookEditionRepository;
import com.thientri.book_area.model.catalog.Book;
import com.thientri.book_area.model.catalog.BookEdition;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class CoverFixUtility {

    @Autowired private MinioClient minioClient;
    @Autowired private BookRepository bookRepository;
    @Autowired private BookEditionRepository bookEditionRepository;

    @Value("${minio.bucket.name:book-area-files}")
    private String bucketName = "book-area-files";

    private static final String GOOGLE_BOOKS_API_KEY = "AIzaSyCO-jtI93iQ1VhTXkg6-lJonLYdO5DjYJg";
    private static final Path FIXED_DIR = Path.of("d:/book-area-code-tay/sach/anhbia_fixed");
    private static final Path OUTPUT_DIR = Path.of("d:/book-area-code-tay");

    // Confirmed good local covers (high-res .png files already correct)
    private static final Set<String> CONFIRMED_GOOD = Set.of(
        "harry-potter-va-hon-da-phu-thuy",
        "mat-biec",
        "nha-gia-kim",
        "rung-na-uy",
        "tuoi-tre-dang-gia-bao-nhieu"
    );

    // User-confirmed wrong/blurry covers that MUST be re-downloaded
    private static final Set<String> CONFIRMED_BAD = Set.of(
        "mua-do",
        "muu-sat",
        "truong-ca-achilles",
        "chim-co-do",
        "ngay-xua-co-mot-chuyen-tinh",
        "sapiens-luoc-su-loai-nguoi",
        "cho-toi-xin-mot-ve-di-tuoi-tho",
        "the-gioi-ngam-cua-ripley"
    );

    // Known very small files (< 10KB) that are certainly broken
    private static final Set<String> CERTAINLY_BROKEN = Set.of(
        "di-tim-le-song",
        "phia-sau-nghi-can-x"
    );

    // Google Books volume IDs for Vietnamese editions (manually verified)
    private static final Map<String, String> GOOGLE_BOOKS_VOLUME_IDS = Map.ofEntries(
        Map.entry("bieu-tuong-that-truyen", "p6rREAAAQBAJ"),
        Map.entry("mat-ma-da-vinci", "IuDNEAAAQBAJ"),
        Map.entry("tet-o-lang-dia-nguc", "hUnoEAAAQBAJ"),
        Map.entry("thao-tung-tam-ly", "JArmEAAAQBAJ"),
        Map.entry("nghe-thuat-tu-duy-chien-luoc", "xT1cEQAAQBAJ"),
        Map.entry("tay-du-ky", "zWiBx7fCsaMC"),
        Map.entry("cay-chuoi-non-di-giay-xanh", "U0ZRAQAAMAAJ"),
        Map.entry("muu-sat", "j6KcEAAAQBAJ"),
        Map.entry("mua-do", "4sKyyCuiubYC"),
        Map.entry("ngay-xua-co-mot-chuyen-tinh", "dfjLEQAAQBAJ"),
        Map.entry("cho-toi-xin-mot-ve-di-tuoi-tho", "37HPEQAAQBAJ"),
        Map.entry("chim-co-do", "9yNYEQAAQBAJ")
    );

    // Open Library ISBNs for books (for cover lookup)
    private static final Map<String, String> OPEN_LIBRARY_ISBNS = Map.ofEntries(
        Map.entry("sapiens-luoc-su-loai-nguoi", "9780062316097"),  // Sapiens English ISBN
        Map.entry("truong-ca-achilles", "9780062060624"),           // Song of Achilles
        Map.entry("the-gioi-ngam-cua-ripley", "9780393324655"),     // Ripley Underground
        Map.entry("di-tim-le-song", "9780807014295"),               // Man's Search for Meaning
        Map.entry("phia-sau-nghi-can-x", "9780312375065"),          // Devotion of Suspect X
        Map.entry("sherlock-holmes-toan-tap", "9780553328257"),     // Complete Sherlock Holmes
        Map.entry("tu-duy-nguoc-dich-chuyen-the-gioi", "9780525429562"), // Originals
        Map.entry("mat-ma-da-vinci", "9780307474278"),              // Da Vinci Code
        Map.entry("bieu-tuong-that-truyen", "9780385504225"),       // Lost Symbol
        Map.entry("mua-do", ""),  // Vietnamese only
        Map.entry("muu-sat", ""), // Vietnamese only
        Map.entry("chim-co-do", "9780061134005"), // The Redbreast
        Map.entry("cho-toi-xin-mot-ve-di-tuoi-tho", ""), // Vietnamese only
        Map.entry("ngay-xua-co-mot-chuyen-tinh", ""),     // Vietnamese only
        Map.entry("cay-chuoi-non-di-giay-xanh", ""),      // Vietnamese only
        Map.entry("nghe-thuat-tu-duy-chien-luoc", "9780393337174"), // Art of Strategy
        Map.entry("tay-du-ky", ""),                        // Vietnamese only
        Map.entry("tet-o-lang-dia-nguc", ""),               // Vietnamese only
        Map.entry("thao-tung-tam-ly", "9780997413816")     // Healing from Hidden Abuse
    );

    static class CoverMapping {
        String slug;
        Long editionId;
        String bookTitle;
        String oldCoverObjectName;
        String newCoverObjectName;
        String localCoverFile;
        String source;
        String confidence;
        String status;
        String note;
    }

    // ==================== PHASE 1: DRY RUN ====================

    @Test
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    void dryRunCoverFix() {
        System.out.println("========== DRY RUN: COVER FIX ANALYSIS ==========\n");

        List<CoverMapping> mappings = new ArrayList<>();
        List<BookEdition> allEditions = bookEditionRepository.findAll();

        for (BookEdition ed : allEditions) {
            CoverMapping m = new CoverMapping();
            m.editionId = ed.getId();
            m.bookTitle = ed.getBook().getTitle();
            m.oldCoverObjectName = ed.getCoverObjectName();

            // Derive slug from fileObjectName or book slug
            String slug = ed.getBook().getSlug();
            m.slug = slug;

            // Determine status
            if (CONFIRMED_GOOD.contains(slug)) {
                m.confidence = "high";
                m.status = "KEEP_AND_RENAME";
                m.source = "local_existing";
                m.note = "Confirmed good high-res cover, will copy to covers/" + slug;
                // Find current file
                Path currentFile = findExistingCover(slug);
                m.localCoverFile = currentFile != null ? currentFile.toString() : "NOT_FOUND";
                String ext = currentFile != null ? getExtension(currentFile.getFileName().toString()) : ".jpg";
                m.newCoverObjectName = "covers/" + slug + ext;
            } else if (CONFIRMED_BAD.contains(slug) || CERTAINLY_BROKEN.contains(slug)) {
                m.confidence = "high";
                m.status = "NEEDS_DOWNLOAD";
                m.source = "pending";
                m.note = CERTAINLY_BROKEN.contains(slug) 
                    ? "File too small (< 10KB), certainly broken"
                    : "User confirmed wrong/blurry";
                m.newCoverObjectName = "covers/" + slug + ".jpg";
                m.localCoverFile = "pending_download";
            } else if (slug.equals("cover4") || slug.equals("cover5") || 
                       m.oldCoverObjectName != null && (m.oldCoverObjectName.equals("cover4.jpg") || m.oldCoverObjectName.equals("cover5.jpg"))) {
                m.confidence = "low";
                m.status = "SKIP";
                m.source = "none";
                m.note = "Legacy book without PDF, skipping";
                m.newCoverObjectName = m.oldCoverObjectName;
                m.localCoverFile = "none";
            } else {
                // Other covers - need verification
                Path currentFile = findExistingCover(slug);
                long fileSize = currentFile != null ? currentFile.toFile().length() : 0;
                if (fileSize < 10_000) {
                    m.confidence = "high";
                    m.status = "NEEDS_DOWNLOAD";
                    m.source = "pending";
                    m.note = "File too small (" + fileSize + " bytes)";
                } else if (fileSize < 30_000) {
                    m.confidence = "medium";
                    m.status = "NEEDS_DOWNLOAD";
                    m.source = "pending";
                    m.note = "File suspiciously small (" + fileSize + " bytes), will try to find better";
                } else {
                    m.confidence = "medium";
                    m.status = "VERIFY_AND_KEEP";
                    m.source = "google_books";
                    m.note = "Size OK (" + fileSize + " bytes), will keep unless better found";
                }
                String ext = currentFile != null ? getExtension(currentFile.getFileName().toString()) : ".jpg";
                m.newCoverObjectName = "sach/anhbia/" + slug + ext;
                m.localCoverFile = currentFile != null ? currentFile.toString() : "NOT_FOUND";
            }

            mappings.add(m);
            System.out.printf("  [%s] %s (%s) | old=%s | new=%s | %s%n",
                m.status, m.slug, m.bookTitle, m.oldCoverObjectName, m.newCoverObjectName, m.note);
        }

        // Summary
        long keepCount = mappings.stream().filter(m -> "KEEP_AND_RENAME".equals(m.status)).count();
        long downloadCount = mappings.stream().filter(m -> "NEEDS_DOWNLOAD".equals(m.status)).count();
        long verifyCount = mappings.stream().filter(m -> "VERIFY_AND_KEEP".equals(m.status)).count();
        long skipCount = mappings.stream().filter(m -> "SKIP".equals(m.status)).count();

        System.out.printf("%n=== SUMMARY ===%n");
        System.out.printf("  KEEP_AND_RENAME: %d%n", keepCount);
        System.out.printf("  NEEDS_DOWNLOAD:  %d%n", downloadCount);
        System.out.printf("  VERIFY_AND_KEEP: %d%n", verifyCount);
        System.out.printf("  SKIP:            %d%n", skipCount);
        System.out.printf("  TOTAL:           %d%n", mappings.size());

        // Write mapping files
        try {
            writeMappingCsv(mappings);
            writeMappingJson(mappings);
            System.out.println("\nMapping files written to: " + OUTPUT_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n========== DRY RUN COMPLETE ==========");
    }

    // ==================== PHASE 2: DOWNLOAD COVERS ====================

    @Test
    void downloadCovers() {
        System.out.println("========== DOWNLOADING COVERS ==========\n");

        try {
            Files.createDirectories(FIXED_DIR);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            // 1. Copy confirmed good covers
            System.out.println("--- Copying confirmed good covers ---");
            for (String slug : CONFIRMED_GOOD) {
                Path src = findExistingCover(slug);
                if (src != null && src.toFile().exists()) {
                    String ext = getExtension(src.getFileName().toString());
                    Path dest = FIXED_DIR.resolve(slug + ext);
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    System.out.printf("  COPIED %s (%d bytes)%n", dest.getFileName(), dest.toFile().length());
                } else {
                    System.err.println("  NOT FOUND: " + slug);
                }
            }

            // 2. Download covers that need fixing
            System.out.println("\n--- Downloading covers that need fixing ---");
            Set<String> needsDownload = new LinkedHashSet<>();
            needsDownload.addAll(CONFIRMED_BAD);
            needsDownload.addAll(CERTAINLY_BROKEN);
            // Also add other covers that are suspiciously small
            for (String slug : List.of("cay-chuoi-non-di-giay-xanh", "bieu-tuong-that-truyen",
                    "mat-ma-da-vinci", "nghe-thuat-tu-duy-chien-luoc", "tay-du-ky",
                    "tet-o-lang-dia-nguc", "thao-tung-tam-ly", "sherlock-holmes-toan-tap",
                    "tu-duy-nguoc-dich-chuyen-the-gioi", "phia-sau-nghi-can-x", "di-tim-le-song")) {
                if (!CONFIRMED_GOOD.contains(slug)) {
                    needsDownload.add(slug);
                }
            }

            for (String slug : needsDownload) {
                System.out.printf("%n  Processing: %s%n", slug);
                boolean downloaded = false;

                // Strategy 1: Open Library (best quality for international books)
                String isbn = OPEN_LIBRARY_ISBNS.getOrDefault(slug, "");
                if (!isbn.isEmpty()) {
                    downloaded = tryOpenLibrary(client, slug, isbn);
                }

                // Strategy 2: Google Books with fife=w600
                if (!downloaded && GOOGLE_BOOKS_VOLUME_IDS.containsKey(slug)) {
                    downloaded = tryGoogleBooksById(client, slug, GOOGLE_BOOKS_VOLUME_IDS.get(slug));
                }

                // Strategy 3: Google Books API search
                if (!downloaded) {
                    downloaded = tryGoogleBooksSearch(client, slug);
                }

                if (!downloaded) {
                    System.err.println("    FAILED: Could not find cover for " + slug);
                }

                Thread.sleep(500); // Rate limiting
            }

            // 3. Verify all files
            System.out.println("\n--- Verification ---");
            File[] fixedFiles = FIXED_DIR.toFile().listFiles();
            if (fixedFiles != null) {
                Arrays.sort(fixedFiles);
                for (File f : fixedFiles) {
                    String status = f.length() > 10_000 ? "OK" : "SUSPICIOUS";
                    System.out.printf("  [%s] %s (%d bytes)%n", status, f.getName(), f.length());
                }
                System.out.printf("%n  Total files: %d%n", fixedFiles.length);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n========== DOWNLOAD COMPLETE ==========");
    }

    // ==================== PHASE 3: EXECUTE (Upload + DB Update) ====================

    @Test
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.test.annotation.Rollback(false)
    void executeCoverFix() {
        System.out.println("========== EXECUTING COVER FIX ==========\n");

        try {
            // 1. Create backup
            System.out.println("--- Creating backup ---");
            List<BookEdition> allEditions = bookEditionRepository.findAll();
            StringBuilder backupCsv = new StringBuilder("edition_id,book_title,old_cover_object_name,new_cover_object_name,updated_at\n");
            StringBuilder rollbackSql = new StringBuilder("-- Rollback cover_object_name updates\n-- Generated: " + LocalDateTime.now() + "\nBEGIN TRANSACTION;\n\n");

            // 2. Process each fixed cover
            File[] fixedFiles = FIXED_DIR.toFile().listFiles();
            if (fixedFiles == null || fixedFiles.length == 0) {
                System.err.println("No files in anhbia_fixed/! Run downloadCovers() first.");
                return;
            }

            Map<String, File> fixedFileMap = new HashMap<>();
            for (File f : fixedFiles) {
                String slug = f.getName().substring(0, f.getName().lastIndexOf('.'));
                fixedFileMap.put(slug, f);
            }

            int uploadCount = 0;
            int updateCount = 0;
            List<String> uploadedObjects = new ArrayList<>();
            List<CoverMapping> mappings = new ArrayList<>();

            for (BookEdition ed : allEditions) {
                String slug = ed.getBook().getSlug();
                File coverFile = fixedFileMap.get(slug);

                if (coverFile == null) {
                    System.out.printf("  SKIP %s (no fixed cover file)%n", slug);
                    continue;
                }

                String ext = getExtension(coverFile.getName());
                String contentType = ext.equals(".png") ? "image/png" : "image/jpeg";
                String newObjectName = "sach/anhbia/" + slug + ext;
                String oldObjectName = ed.getCoverObjectName();

                // 3. Upload to MinIO
                try (FileInputStream fis = new FileInputStream(coverFile)) {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newObjectName)
                            .stream(fis, coverFile.length(), -1)
                            .contentType(contentType)
                            .build());
                    uploadCount++;
                    uploadedObjects.add(newObjectName);
                    System.out.printf("  UPLOADED %s (%d bytes)%n", newObjectName, coverFile.length());
                } catch (Exception e) {
                    System.err.printf("  UPLOAD FAILED %s: %s%n", newObjectName, e.getMessage());
                    continue;
                }

                // 4. Verify upload
                try {
                    StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                            .bucket(bucketName).object(newObjectName).build());
                    System.out.printf("  VERIFIED %s (size=%d, type=%s)%n", newObjectName, stat.size(), stat.contentType());
                } catch (Exception e) {
                    System.err.printf("  VERIFY FAILED %s: %s%n", newObjectName, e.getMessage());
                    continue;
                }

                // 5. Update DB
                backupCsv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        ed.getId(), ed.getBook().getTitle(), oldObjectName, newObjectName, LocalDateTime.now()));
                rollbackSql.append(String.format("UPDATE book_editions SET cover_object_name = '%s' WHERE id = %d; -- %s%n",
                        oldObjectName != null ? oldObjectName.replace("'", "''") : "", ed.getId(), ed.getBook().getTitle()));

                ed.setCoverObjectName(newObjectName);
                bookEditionRepository.save(ed);
                updateCount++;

                CoverMapping m = new CoverMapping();
                m.slug = slug;
                m.editionId = ed.getId();
                m.bookTitle = ed.getBook().getTitle();
                m.oldCoverObjectName = oldObjectName;
                m.newCoverObjectName = newObjectName;
                m.localCoverFile = coverFile.getAbsolutePath();
                m.source = CONFIRMED_GOOD.contains(slug) ? "local_existing" : "downloaded";
                m.confidence = "high";
                m.status = "updated";
                m.note = "Successfully uploaded and DB updated";
                mappings.add(m);

                System.out.printf("  DB UPDATED edition %d: %s -> %s%n", ed.getId(), oldObjectName, newObjectName);
            }

            // 6. Write output files
            rollbackSql.append("\nCOMMIT;\n");
            Files.writeString(OUTPUT_DIR.resolve("rollback_cover_update.sql"), rollbackSql.toString());
            Files.writeString(OUTPUT_DIR.resolve("cover_mapping_backup.csv"), backupCsv.toString());
            writeMappingCsv(mappings);
            writeMappingJson(mappings);

            System.out.printf("%n=== EXECUTION SUMMARY ===%n");
            System.out.printf("  Covers uploaded to MinIO: %d%n", uploadCount);
            System.out.printf("  DB records updated: %d%n", updateCount);
            System.out.printf("  Backup file: cover_mapping_backup.csv%n");
            System.out.printf("  Rollback file: rollback_cover_update.sql%n");

            // Log uploaded objects for potential cleanup
            if (!uploadedObjects.isEmpty()) {
                Files.writeString(OUTPUT_DIR.resolve("uploaded_objects.txt"),
                    String.join("\n", uploadedObjects));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n========== EXECUTION COMPLETE ==========");
    }

    // ==================== HELPER METHODS ====================

    private Path findExistingCover(String slug) {
        Path anhbiaDir = Path.of("d:/book-area-code-tay/sach/anhbia");
        for (String ext : new String[]{".png", ".jpg", ".webp"}) {
            Path p = anhbiaDir.resolve(slug + ext);
            if (p.toFile().exists()) return p;
        }
        return null;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }

    private boolean tryOpenLibrary(HttpClient client, String slug, String isbn) {
        try {
            // Open Library covers: https://covers.openlibrary.org/b/isbn/{isbn}-L.jpg
            String url = "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
            System.out.printf("    Trying Open Library: %s%n", url);

            HttpRequest req = HttpRequest.newBuilder().uri(new URI(url))
                    .header("User-Agent", "Mozilla/5.0").GET().build();
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            if (resp.statusCode() == 200 && resp.body().length > 5000) {
                // Check it's not the "no cover" placeholder (1x1 pixel)
                String contentType = resp.headers().firstValue("Content-Type").orElse("image/jpeg");
                String ext = contentType.contains("png") ? ".png" : ".jpg";
                Path dest = FIXED_DIR.resolve(slug + ext);
                Files.write(dest, resp.body());
                System.out.printf("    SUCCESS Open Library: %s (%d bytes)%n", dest.getFileName(), resp.body().length);
                return true;
            }
        } catch (Exception e) {
            System.err.printf("    Open Library failed: %s%n", e.getMessage());
        }
        return false;
    }

    private boolean tryGoogleBooksById(HttpClient client, String slug, String volumeId) {
        try {
            String url = "http://books.google.com/books/content?id=" + volumeId
                    + "&printsec=frontcover&img=1&zoom=1&fife=w600";
            System.out.printf("    Trying Google Books (ID=%s): %s%n", volumeId, url);

            HttpRequest req = HttpRequest.newBuilder().uri(new URI(url))
                    .header("User-Agent", "Mozilla/5.0").GET().build();
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            if (resp.statusCode() == 200 && resp.body().length > 10_000) {
                String contentType = resp.headers().firstValue("Content-Type").orElse("image/jpeg");
                String ext = contentType.contains("png") ? ".png" : ".jpg";
                Path dest = FIXED_DIR.resolve(slug + ext);
                Files.write(dest, resp.body());
                System.out.printf("    SUCCESS Google Books: %s (%d bytes)%n", dest.getFileName(), resp.body().length);
                return true;
            } else {
                System.out.printf("    Google Books too small: %d bytes%n", resp.body().length);
            }
        } catch (Exception e) {
            System.err.printf("    Google Books failed: %s%n", e.getMessage());
        }
        return false;
    }

    private boolean tryGoogleBooksSearch(HttpClient client, String slug) {
        // Map slug to search query
        Map<String, String> searchQueries = Map.ofEntries(
            Map.entry("mua-do", "Mưa Đỏ Chu Lai"),
            Map.entry("muu-sat", "Mưu Sát Tử Kim Trần"),
            Map.entry("truong-ca-achilles", "Song of Achilles Madeline Miller"),
            Map.entry("chim-co-do", "Redbreast Jo Nesbo"),
            Map.entry("ngay-xua-co-mot-chuyen-tinh", "Ngày Xưa Có Một Chuyện Tình Nguyễn Nhật Ánh"),
            Map.entry("sapiens-luoc-su-loai-nguoi", "Sapiens Yuval Noah Harari"),
            Map.entry("cho-toi-xin-mot-ve-di-tuoi-tho", "Cho Tôi Xin Một Vé Đi Tuổi Thơ Nguyễn Nhật Ánh"),
            Map.entry("the-gioi-ngam-cua-ripley", "Ripley Underground Patricia Highsmith"),
            Map.entry("di-tim-le-song", "Man's Search for Meaning Viktor Frankl"),
            Map.entry("phia-sau-nghi-can-x", "Devotion of Suspect X Keigo Higashino"),
            Map.entry("sherlock-holmes-toan-tap", "Complete Sherlock Holmes Arthur Conan Doyle"),
            Map.entry("tu-duy-nguoc-dich-chuyen-the-gioi", "Originals Adam Grant"),
            Map.entry("cay-chuoi-non-di-giay-xanh", "Cây Chuối Non Đi Giày Xanh Nguyễn Nhật Ánh"),
            Map.entry("tay-du-ky", "Journey to the West"),
            Map.entry("bieu-tuong-that-truyen", "Lost Symbol Dan Brown"),
            Map.entry("mat-ma-da-vinci", "Da Vinci Code Dan Brown"),
            Map.entry("nghe-thuat-tu-duy-chien-luoc", "Art of Strategy Nalebuff Dixit"),
            Map.entry("tet-o-lang-dia-nguc", "Tết Ở Làng Địa Ngục Thảo Trang"),
            Map.entry("thao-tung-tam-ly", "Healing Hidden Abuse Shannon Thomas")
        );

        String query = searchQueries.get(slug);
        if (query == null) return false;

        try {
            String url = "https://www.googleapis.com/books/v1/volumes?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&key=" + GOOGLE_BOOKS_API_KEY + "&maxResults=5";
            System.out.printf("    Trying Google Books Search: %s%n", query);

            HttpRequest req = HttpRequest.newBuilder().uri(new URI(url))
                    .header("User-Agent", "Mozilla/5.0").GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                String body = resp.body();
                // Find first result with a thumbnail
                String thumbnail = extractThumbnailUrl(body);
                if (thumbnail != null) {
                    String hqUrl = thumbnail
                            .replace("&edge=curl", "").replace("edge=curl&", "")
                            + "&fife=w600";
                    return tryGoogleBooksById(client, slug, extractVolumeId(hqUrl, body));
                }
            }
        } catch (Exception e) {
            System.err.printf("    Google Books search failed: %s%n", e.getMessage());
        }
        return false;
    }

    private String extractVolumeId(String url, String body) {
        // Extract first volume ID from the search results
        int idx = body.indexOf("\"id\"");
        if (idx == -1) return "";
        int start = body.indexOf("\"", idx + 4);
        int end = body.indexOf("\"", start + 1);
        return body.substring(start + 1, end);
    }

    private String extractThumbnailUrl(String json) {
        int idx = json.indexOf("\"thumbnail\"");
        if (idx == -1) idx = json.indexOf("\"smallThumbnail\"");
        if (idx == -1) return null;
        int start = json.indexOf("\"", idx + 12);
        if (start == -1) return null;
        int end = json.indexOf("\"", start + 1);
        if (end == -1) return null;
        return json.substring(start + 1, end).replace("\\u0026", "&");
    }

    private void writeMappingCsv(List<CoverMapping> mappings) throws IOException {
        StringBuilder sb = new StringBuilder("pdf_file,slug,edition_id,book_title,old_cover_object_name,new_cover_object_name,local_cover_file,source,confidence,status,note\n");
        for (CoverMapping m : mappings) {
            sb.append(String.format("\"_%.pdf\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                m.slug, m.slug, m.editionId, m.bookTitle,
                m.oldCoverObjectName != null ? m.oldCoverObjectName : "",
                m.newCoverObjectName != null ? m.newCoverObjectName : "",
                m.localCoverFile != null ? m.localCoverFile : "",
                m.source != null ? m.source : "",
                m.confidence, m.status,
                m.note != null ? m.note : ""));
        }
        Files.writeString(OUTPUT_DIR.resolve("cover_mapping.csv"), sb.toString());
    }

    private void writeMappingJson(List<CoverMapping> mappings) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < mappings.size(); i++) {
            CoverMapping m = mappings.get(i);
            sb.append("  {\n");
            sb.append(String.format("    \"slug\": \"%s\",%n", m.slug));
            sb.append(String.format("    \"editionId\": %d,%n", m.editionId));
            sb.append(String.format("    \"bookTitle\": \"%s\",%n", escape(m.bookTitle)));
            sb.append(String.format("    \"oldCoverObjectName\": \"%s\",%n", escape(m.oldCoverObjectName)));
            sb.append(String.format("    \"newCoverObjectName\": \"%s\",%n", escape(m.newCoverObjectName)));
            sb.append(String.format("    \"localCoverFile\": \"%s\",%n", escape(m.localCoverFile)));
            sb.append(String.format("    \"source\": \"%s\",%n", escape(m.source)));
            sb.append(String.format("    \"confidence\": \"%s\",%n", m.confidence));
            sb.append(String.format("    \"status\": \"%s\",%n", m.status));
            sb.append(String.format("    \"note\": \"%s\"%n", escape(m.note)));
            sb.append("  }");
            if (i < mappings.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        Files.writeString(OUTPUT_DIR.resolve("cover_mapping.json"), sb.toString());
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
