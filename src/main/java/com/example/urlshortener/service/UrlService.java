package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlMappingRepository repository;

    private final StringRedisTemplate stringRedisTemplate;


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

    public String getOriginalUrl(String shortCode) {

        log.debug("Looking up short code: {}", shortCode);
        String cacheKey = "short:" + shortCode;

        // 1️⃣ FAST PATH — Redis
        try {
            String cachedUrl = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedUrl != null) {
                log.debug("Cache hit for shortCode={}", shortCode);
                return cachedUrl;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to DB: {}", e.getMessage());
        }

        // 2️⃣ SAFE PATH — DB
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Short code not found: {}", shortCode);
                    return new UrlNotFoundException("Short URL not found: " + shortCode);
                });

        if (!mapping.getIsActive()) {
            log.warn("Inactive URL accessed: {}", shortCode);
            throw new UrlExpiredException("This link is no longer active");
        }

        Instant now = Instant.now();
        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(now)) {
            log.warn("Expired URL accessed: {}", shortCode);
            throw new UrlExpiredException("This link has expired");
        }

        String originalUrl = mapping.getOriginalUrl();

        // 3️⃣ Populate Redis
        try {
            if (mapping.getExpiresAt() != null) {
                long ttlSeconds = Duration.between(now, mapping.getExpiresAt()).getSeconds();


                if (ttlSeconds > 0) {
                    stringRedisTemplate.opsForValue()
                            .set(cacheKey, originalUrl, ttlSeconds, TimeUnit.SECONDS);
                }
            } else {
                // 🔑 NULL expiry → cache WITHOUT TTL
                stringRedisTemplate.opsForValue()
                        .set(cacheKey, originalUrl);
            }
        } catch (Exception e) {
            log.warn("Failed to populate Redis cache: {}", e.getMessage());
        }

        return originalUrl;
    }
}
