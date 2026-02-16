# Design Decisions

## 1. Architecture Overview

The URL Shortener is built as a production-oriented backend system using Spring Boot 3 with a clean layered architecture:

- **Spring Boot** → REST API layer
- **PostgreSQL** → Durable relational storage
- **Redis** → High-speed caching
- **Kafka** → Asynchronous analytics processing
- **Flyway** → Version-controlled database migrations
- **Docker** → Flexible deployment strategy

The system separates core URL resolution logic from analytics processing to ensure low latency and scalability.

## 2. Short Code Generation (Base62)

### Choice: Base62 encoding of `System.nanoTime()`

### Why Base62?

- URL-safe (0-9, A-Z, a-z)
- Compact and efficient
- Human-readable and shareable
- High entropy with small string length

### Character Set

0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz

### Capacity

62^7 = 3,521,614,606,208 possible URLs  
At 1000 URLs/second → ~111 years to exhaust

### Trade-off

- `System.nanoTime()` works well for single-node deployment.
- For distributed systems, Snowflake-style ID generation would be preferred.

## 3. Idempotency Strategy

### Choice: Unique index on `original_url`

Users submitting the same URL receive the same short code.

### Why?

- Prevents duplicate database entries
- Safe retry behavior
- Improved analytics clarity
- Better user experience

### Implementation

CREATE UNIQUE INDEX idx_original_url ON url_mappings(original_url);

### Performance Impact

~1–2ms overhead with proper indexing (acceptable trade-off)

## 4. Data Storage Strategy

### PostgreSQL as Source of Truth

- ACID-compliant
- Strong relational integrity
- Indexed short code lookups
- Reliable long-term storage

### Flyway for Migration Management

- Version-controlled schema evolution
- Environment consistency
- CI/CD friendly

## 5. Redis Caching Strategy (Cache-Aside Pattern)

### Choice: Cache-aside with smart TTL handling

Flow:
1. Check Redis
2. If miss → fetch from DB
3. Store in Redis
4. Return response

### Why?

- <1ms cache hits
- Reduces DB load by 80–90%
- Graceful degradation if Redis fails
- Stateless horizontal scalability

### TTL Strategy

- URLs with expiry → cache until expiry
- Permanent URLs → no TTL (Redis LRU handles eviction)

### Trade-off

- Cache invalidation complexity in distributed scenarios (future scaling consideration)

## 6. Expiry Handling Strategy

### Choice: Read-time expiry validation

Instead of scheduled cleanup jobs, expiry is checked during redirect.

### Why?

- Zero staleness
- No extra writes
- No distributed scheduling complexity
- Stateless across multiple instances

### Trade-off

Expired records remain in database (acceptable due to low storage cost)

## 7. Soft Delete Pattern

### Choice: Mark URLs as inactive instead of deleting

### Why?

- Preserves analytics data
- Prevents short code reuse attacks
- Maintains audit trail
- Allows future reactivation

### Security Example

If `abc123` previously mapped to a banking URL and expires,  
it remains reserved to prevent malicious reuse.

## 8. Rate Limiting Strategy

### Choice: IP-based token bucket using Redis

Configuration:
- 100 requests per minute per IP
- Fail-open behavior if Redis is unavailable

### Why?

- Prevents abuse and scraping
- Ensures fair usage
- Protects infrastructure costs
- Distributed-safe via Redis

## 9. Kafka for Analytics (Fire-and-Forget)

### Choice: Asynchronous event publishing

Redirect requests publish click events to Kafka without blocking.

### Why?

- Non-blocking redirects
- Decoupled analytics pipeline
- Scalable event processing
- Fail-open design (core redirect unaffected if Kafka fails)

### Trade-off

- Adds operational complexity
- Single broker setup not production-cluster ready

## 10. Configuration Strategy

### Choice: Environment-variable-driven configuration

Example:
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}

### Benefits

- Works locally without extra setup
- Docker-ready
- Cloud-ready
- No profile duplication
- Clear separation between code and environment

The application supports:
1. Local app + Docker infrastructure mode
2. Full Docker deployment mode

## 11. Failure Handling Philosophy

The system follows a **fail-open approach** for non-critical dependencies:

- If Redis fails → fallback to database
- If Kafka fails → redirect still succeeds
- If analytics consumer fails → no impact on core URL resolution

Core redirect functionality always takes priority over analytics.

## 12. Design Philosophy

This system prioritizes:

- Low-latency redirects
- Clear separation of concerns
- Stateless scalability
- Safe retries and idempotency
- Production-oriented thinking
- Clean configuration strategy

The goal is not just to build a working URL shortener, but to demonstrate sound backend architectural decision-making aligned with real-world system design principles.
