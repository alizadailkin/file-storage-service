package com.filestorage.security;

import com.filestorage.entity.ApiKey;
import com.filestorage.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        log.info("=== REQUEST START ===");
        log.info("Path: {}", requestPath);
        log.info("Method: {}", request.getMethod());


        if (requestPath.startsWith("/api/keys/generate")) {
            log.info("Public endpoint, skipping auth");
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        log.info("API Key Header (X-API-Key): {}", apiKey != null ? "EXISTS (length: " + apiKey.length() + ")" : "NULL");

        if (apiKey == null || apiKey.isEmpty()) {
            log.error("REJECTED: Missing API key");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"API Key is required\"}");
            return;
        }

        log.info("Fetching all API keys from database...");
        List<ApiKey> allKeys = apiKeyRepository.findAll();
        log.info("Found {} keys in database", allKeys.size());

        Optional<ApiKey> validKey = allKeys.stream()
                .peek(key -> log.info("Checking key: name='{}', active={}", key.getName(), key.getIsActive()))
                .filter(ApiKey::getIsActive)
                .peek(key -> log.info("Key '{}' is active, checking hash...", key.getName()))
                .filter(key -> {
                    boolean matches = passwordEncoder.matches(apiKey, key.getKeyHash());
                    log.info("Key '{}' match result: {}", key.getName(), matches);
                    return matches;
                })
                .findFirst();

        if (validKey.isEmpty()) {
            log.error("REJECTED: Invalid API key (no match found)");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API Key\"}");
            return;
        }

        ApiKey key = validKey.get();
        log.info("SUCCESS: Authenticated as '{}'", key.getName());


        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                key.getName(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_API_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);


        key.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(key);

        request.setAttribute("apiKey", key);

        filterChain.doFilter(request, response);
    }
}