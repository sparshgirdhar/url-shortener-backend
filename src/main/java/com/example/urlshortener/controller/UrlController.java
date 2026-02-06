package com.example.urlshortener.controller;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    public UrlResponse shorten(@Valid @RequestBody ShortenRequest request) {

        UrlMapping mapping = urlService.createShortUrl(request.getOriginalUrl());

        return new UrlResponse(
                mapping.getShortCode(),
                "http://short.ly/" + mapping.getShortCode()
        );
    }
}
