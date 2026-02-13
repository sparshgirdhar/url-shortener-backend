package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private UrlService urlService;

    @Test
    void shouldCreateNewShortUrl() {
        // Given
        when(repository.findByOriginalUrl(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        UrlMapping result = urlService.createShortUrl("https://www.test.com");

        // Then
        assertNotNull(result);
        assertNotNull(result.getShortCode());
        assertEquals("https://www.test.com", result.getOriginalUrl());
        assertTrue(result.getIsActive());
        verify(repository).save(any());
    }

    @Test
    void shouldReturnExistingUrlForIdempotency() {
        // Given
        UrlMapping existing = UrlMapping.builder()
                .id(1L)
                .shortCode("abc123")
                .originalUrl("https://www.existing.com")
                .createdAt(Instant.now())
                .isActive(true)
                .build();

        when(repository.findByOriginalUrl("https://www.existing.com"))
                .thenReturn(Optional.of(existing));

        // When
        UrlMapping result = urlService.createShortUrl("https://www.existing.com");

        // Then
        assertEquals("abc123", result.getShortCode());
        verify(repository, never()).save(any()); // Should NOT create new
    }
}
