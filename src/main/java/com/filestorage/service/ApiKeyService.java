package com.filestorage.service;

import com.filestorage.dto.ApiKeyRequest;
import com.filestorage.dto.ApiKeyResponse;
import com.filestorage.entity.ApiKey;
import com.filestorage.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final int API_KEY_LENGTH = 32;

    public ApiKeyResponse generateApiKey(ApiKeyRequest request) {
        if (apiKeyRepository.existsByName(request.getName())) {
            throw new RuntimeException("API Key with this name already exists");
        }


        String rawApiKey = generateRandomKey();
        String hashedKey = passwordEncoder.encode(rawApiKey);


        ApiKey apiKey = new ApiKey(hashedKey, request.getName());
        apiKey = apiKeyRepository.save(apiKey);

        log.info("Generated new API key: {}", request.getName());


        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getName(),
                rawApiKey,
                maskKey(rawApiKey),
                apiKey.getCreatedAt(),
                apiKey.getLastUsedAt(),
                apiKey.getIsActive()
        );
    }

    public List<ApiKeyResponse> getAllApiKeys() {
        return apiKeyRepository.findAll().stream()
                .map(key -> new ApiKeyResponse(
                        key.getId(),
                        key.getName(),
                        "****",  // Masked
                        key.getCreatedAt(),
                        key.getLastUsedAt(),
                        key.getIsActive()
                ))
                .collect(Collectors.toList());
    }

    public void deleteApiKey(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API Key not found"));
        apiKeyRepository.delete(apiKey);
        log.info("Deleted API key: {}", apiKey.getName());
    }

    private String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[API_KEY_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String maskKey(String key) {
        if (key.length() < 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}