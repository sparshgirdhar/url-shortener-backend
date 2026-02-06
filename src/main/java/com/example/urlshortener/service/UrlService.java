package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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
                            .isActive(true)
                            .build();

                    return repository.save(entity);
                });
    }
}
