package com.filestorage.controller;

import com.filestorage.dto.ApiKeyRequest;
import com.filestorage.dto.ApiKeyResponse;
import com.filestorage.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;


    @PostMapping("/generate")
    public ResponseEntity<ApiKeyResponse> generateApiKey(@Valid @RequestBody ApiKeyRequest request) {
        ApiKeyResponse response = apiKeyService.generateApiKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/list")
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys() {
        List<ApiKeyResponse> keys = apiKeyService.getAllApiKeys();
        return ResponseEntity.ok(keys);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.noContent().build();
    }
}