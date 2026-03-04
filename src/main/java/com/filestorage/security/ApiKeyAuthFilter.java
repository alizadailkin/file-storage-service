package com.filestorage.security;

import com.filestorage.entity.ApiKey;
import com.filestorage.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Allow API key generation endpoint without authentication
        if (requestPath.startsWith("/api/keys/generate")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Missing API key for request: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"API Key is required\"}");
            return;
        }

        // Verify API key
        Optional<ApiKey> validKey = apiKeyRepository.findAll().stream()
                .filter(key -> key.getIsActive())
                .filter(key -> passwordEncoder.matches(apiKey, key.getKeyHash()))
                .findFirst();

        if (validKey.isEmpty()) {
            log.warn("Invalid API key for request: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid API Key\"}");
            return;
        }

        // Update last used timestamp
        ApiKey key = validKey.get();
        key.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(key);

        // Store API key in request attribute for later use
        request.setAttribute("apiKey", key);

        filterChain.doFilter(request, response);
    }
}