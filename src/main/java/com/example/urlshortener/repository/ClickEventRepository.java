package com.example.urlshortener.repository;

import com.example.urlshortener.domain.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    // For future analytics
    long countByShortCode(String shortCode);

    @Query("SELECT COUNT(DISTINCT c.ipAddress) FROM ClickEvent c WHERE c.shortCode = :shortCode")
    long countUniqueVisitors(String shortCode);
}