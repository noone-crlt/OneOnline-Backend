package com.thientri.book_area.config;

import java.util.HashSet;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.model.user.Role;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.model.user.UserStatus;
import com.thientri.book_area.repository.user.RoleRepository;
import com.thientri.book_area.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking and seeding database...");

        // Seed Roles
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            log.info("Creating ADMIN role");
            return roleRepository.save(Role.builder().name("ADMIN").build());
        });

        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            log.info("Creating USER role");
            return roleRepository.save(Role.builder().name("USER").build());
        });

        // Seed Admin User
        String adminEmail = "admin@oneonline.vn";
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Creating default admin account...");
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên")
                    .phone("0123456789")
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>())
                    .build();
            adminUser.getRoles().add(adminRole);
            userRepository.save(adminUser);
            log.info("Admin account created: {} / admin123", adminEmail);
        } else {
            log.info("Admin account already exists.");
        }
    }
}
