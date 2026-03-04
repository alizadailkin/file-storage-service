package com.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String apiKey;  // Only shown once during creation
    private String maskedKey;  // Masked version for listing
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private Boolean isActive;

    public ApiKeyResponse(Long id, String name, String maskedKey,
                          LocalDateTime createdAt, LocalDateTime lastUsedAt, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.maskedKey = maskedKey;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.isActive = isActive;
    }
}