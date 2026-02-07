package com.example.urlshortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.urlshortener.repository.UrlMappingRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlCleanupService {

    private final UrlMappingRepository repository;

    @Scheduled(cron = "0 0 2 * * 0")
    public void cleanupOldExpiredUrls() {
        log.info("Starting expired URL cleanup");

        Instant cutoff = Instant.now().minus(365, ChronoUnit.DAYS);
        int deleted = repository.deleteExpiredOlderThan(cutoff);

        log.info("Expired URL cleanup completed. Deleted rows: {}", deleted);
    }
}
