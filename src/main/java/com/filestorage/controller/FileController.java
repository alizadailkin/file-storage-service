package com.filestorage.controller;

import com.filestorage.dto.FileMetadataResponse;
import com.filestorage.dto.FileUploadResponse;
import com.filestorage.entity.ApiKey;
import com.filestorage.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final StorageService storageService;

    /**
     * Upload file
     * Requires valid API Key in X-API-Key header
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Get API key from request attribute (set by ApiKeyAuthFilter)
        ApiKey apiKey = (ApiKey) request.getAttribute("apiKey");

        FileUploadResponse response = storageService.uploadFile(file, apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Download file
     * Requires valid API Key in X-API-Key header
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        FileMetadataResponse metadata = storageService.getFileMetadata(fileId);
        Resource resource = storageService.downloadFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalName() + "\"")
                .body(resource);
    }

    /**
     * Get file metadata
     * Requires valid API Key in X-API-Key header
     */
    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(@PathVariable String fileId) {
        FileMetadataResponse metadata = storageService.getFileMetadata(fileId);
        return ResponseEntity.ok(metadata);
    }

    /**
     * List all files
     * Requires valid API Key in X-API-Key header
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileMetadataResponse>> listFiles() {
        List<FileMetadataResponse> files = storageService.listAllFiles();
        return ResponseEntity.ok(files);
    }

    /**
     * Delete file
     * Requires valid API Key in X-API-Key header
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        storageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}