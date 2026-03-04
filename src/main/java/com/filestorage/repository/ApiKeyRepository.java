package com.filestorage.repository;

import com.filestorage.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    Optional<ApiKey> findByKeyHashAndIsActiveTrue(String keyHash);

    boolean existsByName(String name);
}