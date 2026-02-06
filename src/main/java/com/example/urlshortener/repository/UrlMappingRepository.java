package com.example.urlshortener.repository;

import com.example.urlshortener.domain.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCodeAndIsActiveTrue(String shortCode);
}
