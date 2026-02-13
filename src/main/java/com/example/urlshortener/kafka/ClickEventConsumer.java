package com.example.urlshortener.kafka;

import com.example.urlshortener.domain.ClickEvent;
import com.example.urlshortener.dto.ClickEventDto;
import com.example.urlshortener.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

    private final ClickEventRepository clickEventRepository;

    @KafkaListener(topics = "url.clicked", groupId = "analytics-group")
    public void consumeClickEvent(ClickEventDto dto) {
        try {
            log.debug("Consuming click event for shortCode={}", dto.getShortCode());

            // Save to database
            ClickEvent entity = ClickEvent.builder()
                    .shortCode(dto.getShortCode())
                    .clickedAt(dto.getClickedAt())
                    .ipAddress(dto.getIpAddress())
                    .userAgent(dto.getUserAgent())
                    .referrer(dto.getReferrer())
                    .build();

            clickEventRepository.save(entity);

            log.info("Saved click event for shortCode={}", dto.getShortCode());

        } catch (Exception e) {
            log.error("Failed to process click event: {}", e.getMessage(), e);
            // In production: send to Dead Letter Queue (DLQ)
        }
    }
}