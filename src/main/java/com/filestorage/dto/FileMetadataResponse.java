package com.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataResponse {
    private Long id;
    private String fileId;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
    private Long downloadCount;
    private LocalDateTime lastAccessedAt;
    private String uploadedBy;
}