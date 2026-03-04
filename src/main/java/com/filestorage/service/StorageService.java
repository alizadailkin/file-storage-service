package com.filestorage.service;

import com.filestorage.dto.FileMetadataResponse;
import com.filestorage.dto.FileUploadResponse;
import com.filestorage.entity.ApiKey;
import com.filestorage.entity.FileMetadata;
import com.filestorage.exception.FileNotFoundException;
import com.filestorage.exception.FileStorageException;
import com.filestorage.repository.FileMetadataRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.bucket}")
    private String bucketName;

    public FileUploadResponse uploadFile(MultipartFile file, ApiKey apiKey) {
        try {
            String fileId = UUID.randomUUID().toString();
            String originalName = file.getOriginalFilename();
            String storedName = fileId + "_" + originalName;
            String objectName = "files/" + storedName;

            // Upload to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Save metadata to database
            FileMetadata metadata = new FileMetadata();
            metadata.setFileId(fileId);
            metadata.setOriginalName(originalName);
            metadata.setStoredName(storedName);
            metadata.setFileSize(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setBucketName(bucketName);
            metadata.setObjectName(objectName);
            metadata.setUploadedByKey(apiKey);
            metadata = fileMetadataRepository.save(metadata);

            log.info("File uploaded successfully: {}", fileId);

            return new FileUploadResponse(
                    metadata.getFileId(),
                    metadata.getOriginalName(),
                    metadata.getFileSize(),
                    metadata.getContentType(),
                    metadata.getUploadedAt(),
                    "/api/files/" + fileId
            );

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload file", e);
        }
    }

    public Resource downloadFile(String fileId) {
        try {
            FileMetadata metadata = fileMetadataRepository.findByFileId(fileId)
                    .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(metadata.getBucketName())
                            .object(metadata.getObjectName())
                            .build()
            );

            metadata.incrementDownloadCount();
            fileMetadataRepository.save(metadata);

            log.info("File downloaded: {}", fileId);

            return new InputStreamResource(stream);

        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to download file", e);
        }
    }

    public FileMetadataResponse getFileMetadata(String fileId) {
        FileMetadata metadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        return new FileMetadataResponse(
                metadata.getId(),
                metadata.getFileId(),
                metadata.getOriginalName(),
                metadata.getFileSize(),
                metadata.getContentType(),
                metadata.getUploadedAt(),
                metadata.getDownloadCount(),
                metadata.getLastAccessedAt(),
                metadata.getUploadedByKey().getName()
        );
    }

    public List<FileMetadataResponse> listAllFiles() {
        return fileMetadataRepository.findAll().stream()
                .map(metadata -> new FileMetadataResponse(
                        metadata.getId(),
                        metadata.getFileId(),
                        metadata.getOriginalName(),
                        metadata.getFileSize(),
                        metadata.getContentType(),
                        metadata.getUploadedAt(),
                        metadata.getDownloadCount(),
                        metadata.getLastAccessedAt(),
                        metadata.getUploadedByKey().getName()
                ))
                .collect(Collectors.toList());
    }

    public void deleteFile(String fileId) {
        try {
            FileMetadata metadata = fileMetadataRepository.findByFileId(fileId)
                    .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

            // Delete from MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(metadata.getBucketName())
                            .object(metadata.getObjectName())
                            .build()
            );

            // Delete metadata from database
            fileMetadataRepository.delete(metadata);

            log.info("File deleted: {}", fileId);

        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to delete file", e);
        }
    }
}