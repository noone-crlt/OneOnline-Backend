package com.thientri.book_area.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.user.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // Máy quét JWT các request gửi đến
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Kiểm tra request có JWT và Header có bắt đầu bằng "Bearer " hay không
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7).trim();
        if (jwt.isEmpty() || "null".equalsIgnoreCase(jwt) || "undefined".equalsIgnoreCase(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Chuỗi JWT nhận được: {}", jwt);

        try {
            final String userEmail = jwtService.extractEmail(jwt);
            log.debug("Email giải mã được: {}", userEmail);

            if (userEmail != null
                    && !userEmail.isBlank()
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Bắt đầu xác thực cho: {}", userEmail);

                User userAuth = userRepository.findByEmail(userEmail)
                        .orElse(null);

                if (userAuth != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userAuth, null, userAuth.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("Bỏ qua JWT không hợp lệ: {}", exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
