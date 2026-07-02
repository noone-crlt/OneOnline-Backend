package com.thientri.book_area.controller.engagement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.model.user.User;
import com.thientri.book_area.service.engagement.LibraryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {
    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<?> getLibrary(@AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(libraryService.getLibrary(user, page, size));
    }
}
