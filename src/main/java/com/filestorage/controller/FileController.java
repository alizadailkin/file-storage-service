package com.filestorage.controller;

import com.filestorage.dto.FileMetadataResponse;
import com.filestorage.dto.FileUploadResponse;
import com.filestorage.entity.ApiKey;
import com.filestorage.entity.FileMetadata;
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


    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        log.info("=== UPLOAD START ===");
        log.info("File name: {}", file.getOriginalFilename());
        log.info("File size: {}", file.getSize());
        log.info("Content type: {}", file.getContentType());

        if (file.isEmpty()) {
            log.error("File is empty!");
            return ResponseEntity.badRequest().build();
        }

        ApiKey apiKey = (ApiKey) request.getAttribute("apiKey");
        log.info("Uploading with API key: {}", apiKey.getName());

        FileUploadResponse response = storageService.uploadFile(file, apiKey);

        log.info("=== UPLOAD SUCCESS === File ID: {}", response.getFileId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        log.info("=== DOWNLOAD START === File ID: {}", fileId);

        Resource resource = storageService.downloadFile(fileId);
        FileMetadataResponse metadata = storageService.getFileMetadata(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(resource);
    }


    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(@PathVariable String fileId) {
        log.info("=== METADATA REQUEST === File ID: {}", fileId);

        FileMetadataResponse metadata = storageService.getFileMetadata(fileId);
        return ResponseEntity.ok(metadata);
    }


    @GetMapping("/list")
    public ResponseEntity<List<FileMetadataResponse>> listFiles() {
        log.info("=== LIST FILES ===");

        List<FileMetadataResponse> files = storageService.listAllFiles();
        return ResponseEntity.ok(files);
    }


    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        log.info("=== DELETE START === File ID: {}", fileId);

        storageService.deleteFile(fileId);

        log.info("=== DELETE SUCCESS === File ID: {}", fileId);
        return ResponseEntity.noContent().build();
    }
}