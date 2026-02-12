package com.example.urlshortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate:";
    private static final long MAX_REQUESTS = 100; // requests per window
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /**
     * Check if request is allowed (token bucket algorithm)
     *
     * @param key Identifier (e.g., IP address)
     * @return true if allowed, false if rate limited
     */
    public boolean isAllowed(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            // Increment counter
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount == null) {
                return true; // Redis error, allow request
            }

            // First request - set expiry
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, WINDOW);
            }

            // Check if under limit
            boolean allowed = currentCount <= MAX_REQUESTS;

            if (!allowed) {
                log.warn("Rate limit exceeded for key={}, count={}", key, currentCount);
            }

            return allowed;

        } catch (Exception e) {
            log.warn("Rate limit check failed: {}", e.getMessage());
            return true; // Fail open - allow request if Redis down
        }
    }

    /**
     * Get remaining requests for a key
     */
    public long getRemainingRequests(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            String count = redisTemplate.opsForValue().get(redisKey);
            long currentCount = count != null ? Long.parseLong(count) : 0;
            return Math.max(0, MAX_REQUESTS - currentCount);
        } catch (Exception e) {
            return MAX_REQUESTS;
        }
    }
}
