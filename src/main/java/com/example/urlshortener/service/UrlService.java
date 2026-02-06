package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlMappingRepository repository;

    @Transactional
    public UrlMapping createShortUrl(String originalUrl) {

        String shortCode = Base62Encoder.encode(System.nanoTime());

        UrlMapping entity = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .createdAt(Instant.now())
                .isActive(true)
                .build();

        return repository.save(entity);
    }
}