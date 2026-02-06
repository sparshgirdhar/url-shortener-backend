package com.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UrlResponse {
    private String shortCode;
    private String shortUrl;
}
