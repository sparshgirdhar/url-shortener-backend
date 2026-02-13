package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.UrlResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateShortUrl() {
        // Given
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.example.com");

        // When
        ResponseEntity<UrlResponse> response = restTemplate.postForEntity(
                "/api/shorten",
                request,
                UrlResponse.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getShortCode());
        assertNotNull(response.getBody().getShortUrl());
    }

    @Test
    void shouldRedirectToOriginalUrl() {
        // Given - Create URL first
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.google.com");

        ResponseEntity<UrlResponse> createResponse = restTemplate.postForEntity(
                "/api/shorten",
                request,
                UrlResponse.class
        );

        String shortCode = createResponse.getBody().getShortCode();

        // When - Try to redirect
        ResponseEntity<String> redirectResponse = restTemplate.getForEntity(
                "/" + shortCode,
                String.class
        );

        // Then - Should get redirect (Spring follows it automatically)
        assertEquals(HttpStatus.OK, redirectResponse.getStatusCode());
    }

    @Test
    void shouldReturnIdempotentResults() {
        // Given
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.idempotent-test.com");

        // When - Create same URL twice
        ResponseEntity<UrlResponse> response1 = restTemplate.postForEntity(
                "/api/shorten",
                request,
                UrlResponse.class
        );

        ResponseEntity<UrlResponse> response2 = restTemplate.postForEntity(
                "/api/shorten",
                request,
                UrlResponse.class
        );

        // Then - Should return same short code
        assertEquals(response1.getBody().getShortCode(), response2.getBody().getShortCode());
    }

    @Test
    void shouldReturn404ForInvalidShortCode() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/invalid-code-123",
                String.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}