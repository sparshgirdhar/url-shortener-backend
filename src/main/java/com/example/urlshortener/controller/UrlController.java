package com.example.urlshortener.controller;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    public UrlResponse shorten(@Valid @RequestBody ShortenRequest request) {

        UrlMapping mapping = urlService.createShortUrl(request.getOriginalUrl());

        return new UrlResponse(
                mapping.getShortCode(),
                "http://short.ly/" + mapping.getShortCode()
        );
    }

    /**
     * Redirects short URL to original URL
     *
     * GET /abc123 -> 302 Redirect to https://google.com
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        // Get original URL from service
        String originalUrl = urlService.getOriginalUrl(shortCode);

        // Return 302 redirect
        return ResponseEntity
                .status(HttpStatus.FOUND)  // 302 status code
                .location(URI.create(originalUrl))
                .build();
    }
}
