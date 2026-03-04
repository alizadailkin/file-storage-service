package com.filestorage.repository;

import com.filestorage.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFileId(String fileId);

    List<FileMetadata> findByUploadedByKeyId(Long apiKeyId);

    List<FileMetadata> findByContentType(String contentType);

    boolean existsByFileId(String fileId);
}