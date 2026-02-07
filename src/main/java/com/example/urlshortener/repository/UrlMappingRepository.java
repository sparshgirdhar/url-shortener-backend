package com.example.urlshortener.repository;

import com.example.urlshortener.domain.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByOriginalUrl(String originalUrl);

    Optional<UrlMapping> findByShortCode(String shortCode);

    void deleteByExpiresAtBefore(Instant expiryDate);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM UrlMapping u
        WHERE u.expiresAt IS NOT NULL
          AND u.expiresAt < :cutoff
    """)
    int deleteExpiredOlderThan(@Param("cutoff") Instant cutoff);

}
