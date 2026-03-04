package com.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileId;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}