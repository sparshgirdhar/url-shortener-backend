package com.example.urlshortener.kafka;

import com.example.urlshortener.dto.ClickEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventProducer {

    private final KafkaTemplate<String, ClickEventDto> kafkaTemplate;

    private static final String TOPIC = "url.clicked";

    /**
     * Publish click event to Kafka (async, fire-and-forget)
     */
    @Async
    public void publishClickEvent(ClickEventDto event) {
        try {
            kafkaTemplate.send(TOPIC, event.getShortCode(), event);
            log.debug("Published click event for shortCode={}", event.getShortCode());
        } catch (Exception e) {
            log.error("Failed to publish click event: {}", e.getMessage());
            // Don't fail the redirect!
        }
    }
}