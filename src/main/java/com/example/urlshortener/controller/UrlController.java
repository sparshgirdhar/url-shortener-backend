package com.example.urlshortener.controller;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.exception.RateLimitExceededException;
import com.example.urlshortener.service.RateLimitService;
import com.example.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final RateLimitService rateLimitService;

    @PostMapping("/api/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    public UrlResponse shorten(@Valid @RequestBody ShortenRequest request, HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        if (!rateLimitService.isAllowed(clientIp)) {
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }

        UrlMapping mapping = urlService.createShortUrl(request.getOriginalUrl());

        return new UrlResponse(
                mapping.getShortCode(),
                "http://short.ly/" + mapping.getShortCode()
        );
    }

    // Helper to get client IP
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Redirects short URL to original URL
     *
     * GET /abc123 -> 302 Redirect to https://google.com
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        // Get original URL from service
        String originalUrl = urlService.getOriginalUrl(shortCode, request);

        // Return 302 redirect
        return ResponseEntity
                .status(HttpStatus.FOUND)  // 302 status code
                .location(URI.create(originalUrl))
                .build();
    }
}
