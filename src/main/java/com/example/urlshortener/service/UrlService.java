package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlMappingRepository repository;

    private static final String BASE_URL = "http://localhost:8080/";

    @Transactional
    public UrlMapping createShortUrl(String originalUrl) {
        // Check if already exists (FAST due to index)
        return repository.findByOriginalUrl(originalUrl)
                .orElseGet(() -> {
                    // Only generate + insert if doesn't exist
                    String shortCode = Base62Encoder.encode(System.nanoTime());

                    UrlMapping entity = UrlMapping.builder()
                            .originalUrl(originalUrl)
                            .shortCode(shortCode)
                            .createdAt(Instant.now())
                            .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                            .isActive(true)
                            .build();

                    return repository.save(entity);
                });
    }

    /**
     * Gets the original URL for redirect
     *
     * @param shortCode The short code from URL path
     * @return Original URL to redirect to
     * @throws UrlNotFoundException if short code doesn't exist
     * @throws UrlExpiredException if URL has expired
     */
    @Transactional(readOnly = true) // Performance Optimization
    public String getOriginalUrl(String shortCode) {
        log.debug("Looking up short code: {}", shortCode);

        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Short code not found: {}", shortCode);
                    return new UrlNotFoundException("Short URL not found: " + shortCode);
                });

        if (!mapping.getIsActive()) {
            log.warn("Inactive URL accessed: {}", shortCode);
            throw new UrlExpiredException("This link is no longer active");
        }

        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Expired URL accessed: {}", shortCode);
            throw new UrlExpiredException("This link has expired");
        }

        return mapping.getOriginalUrl();
    }
}
