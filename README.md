# URL Shortener 🚀

A high-performance URL shortening service built with Spring Boot, featuring Redis caching, Kafka-based analytics, and comprehensive rate limiting. Designed to handle millions of requests per day with sub-5ms redirect latency.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-3.6-black.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

## ✨ Features

### Core Functionality
- ✅ **URL Shortening** - Convert long URLs to short, memorable codes (Base62 encoding)
- ✅ **Fast Redirects** - <5ms latency with Redis caching
- ✅ **URL Expiry** - Auto-expire URLs after 30 days (configurable)
- ✅ **Click Analytics** - Track clicks, referrers, user agents, and IP addresses
- ✅ **Idempotency** - Same URL always returns the same short code

### Performance & Reliability
- ✅ **Redis Caching** - 10x faster redirects with intelligent cache-aside pattern
- ✅ **Rate Limiting** - 100 requests/minute per IP (token bucket algorithm)
- ✅ **Async Analytics** - Non-blocking click tracking with Kafka
- ✅ **Graceful Degradation** - Service continues even if Redis/Kafka are down
- ✅ **Horizontal Scaling** - Stateless design for easy scaling

### Data Management
- ✅ **Read-Time Expiry** - Zero-staleness expiry checks (no cron jobs needed)
- ✅ **Database Indexing** - Optimized B-tree indexes for fast lookups
- ✅ **Soft Delete** - Preserve analytics and prevent short code reuse attacks
- ✅ **Database Migrations** - Version-controlled schema with Flyway

## 🏗️ Architecture

### High-Level Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│    Load Balancer (Future)       │
└──────────────┬──────────────────┘
               │
     ┌─────────┴─────────┐
     ▼                   ▼
┌──────────┐      ┌──────────┐
│  App     │      │  App     │  (Horizontal Scaling)
│ Server 1 │      │ Server N │
└────┬─────┘      └────┬─────┘
     │                 │
     └────────┬────────┘
              │
    ┌─────────┼─────────┐
    ▼         ▼         ▼
┌────────┐ ┌─────┐ ┌──────────┐
│ Redis  │ │ DB  │ │  Kafka   │
│ Cache  │ │     │ │ Analytics│
└────────┘ └─────┘ └──────────┘
```

### Request Flow

#### Write Path (URL Creation)
```
1. POST /api/shorten
   ↓
2. Check Idempotency (DB lookup)
   ↓
3. Generate Short Code (Base62)
   ↓
4. Save to PostgreSQL
   ↓
5. Return Short URL
```

#### Read Path (Redirect)
```
1. GET /{shortCode}
   ↓
2. Check Redis Cache (90% hit rate)
   ├─ HIT: Return URL (<1ms)
   └─ MISS: Query Database
      ↓
   3. Validate Expiry (read-time)
      ↓
   4. Cache in Redis
      ↓
   5. Publish Click Event to Kafka (async)
      ↓
   6. 302 Redirect to Original URL
```

#### Analytics Path (Async)
```
1. Click Event Published to Kafka
   ↓
2. Kafka Consumer Processes Event
   ↓
3. Save to click_events Table
   ↓
4. Analytics Available for Queries
```

## 🛠️ Tech Stack

### Backend
- **Java 17** - LTS version with modern features
- **Spring Boot 3.5** - Framework for production-ready applications
- **Spring Data JPA** - Database access with Hibernate
- **Spring Data Redis** - Caching layer integration
- **Spring Kafka** - Event streaming for analytics

### Databases & Caching
- **PostgreSQL 15** - Primary data store (ACID compliance)
- **Redis 7** - In-memory cache for hot URLs
- **Flyway** - Database migration management

### Message Queue
- **Apache Kafka** - Event streaming platform
- **Zookeeper** - Kafka coordination

### DevOps & Tools
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Maven** - Build automation
- **Lombok** - Boilerplate reduction

## 🚀 Getting Started

### Prerequisites

- **Docker** & **Docker Compose** (for containerized deployment)
- **Java 17+** (for local development)
- **Maven 3.6+** (for local development)

### Quick Start (Docker - Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/url-shortener.git
cd url-shortener

# Start all services (app, postgres, redis, kafka)
docker-compose up --build

# Application will be available at http://localhost:8080
```

That's it! All services (PostgreSQL, Redis, Kafka, Zookeeper) start automatically.

### Local Development Setup

```bash
# Start infrastructure only
docker compose -f docker-compose.local.yml up
or 
docker-compose up postgres redis kafka zookeeper

# Run application locally
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Endpoints

#### 1. Create Short URL

**Request:**
```bash
POST /api/shorten
Content-Type: application/json

{
  "originalUrl": "https://www.example.com/very/long/url/path"
}
```

**Response:**
```json
{
  "shortCode": "abc123XY",
  "shortUrl": "http://localhost:8080/abc123XY"
}
```

**Status Codes:**
- `201 Created` - URL successfully shortened
- `400 Bad Request` - Invalid URL format
- `429 Too Many Requests` - Rate limit exceeded

#### 2. Redirect to Original URL

**Request:**
```bash
GET /{shortCode}
```

**Response:**
```
HTTP/1.1 302 Found
Location: https://www.example.com/very/long/url/path
```

**Status Codes:**
- `302 Found` - Redirect successful
- `404 Not Found` - Short code doesn't exist
- `410 Gone` - URL has expired

#### 3. Health Check

**Request:**
```bash
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

## 💻 Development

### Project Structure

```
src/
├── main/
│   ├── java/com/example/urlshortener/
│   │   ├── config/          # Spring configuration
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access (JPA)
│   │   ├── domain/          # JPA entities
│   │   ├── dto/             # Request/response objects
│   │   ├── kafka/           # Kafka producer/consumer
│   │   ├── exception/       # Custom exceptions
│   │   └── util/            # Utilities (Base62)
│   └── resources/
│       ├── db/migration/    # Flyway SQL scripts
│       └── application.properties
└── test/                    # Unit & integration tests
```

### Database Schema

**url_mappings** (Core table)
```sql
Column       | Type      | Description
-------------|-----------|----------------------------------
id           | BIGSERIAL | Primary key
short_code   | VARCHAR   | Unique short identifier
original_url | TEXT      | Original long URL
created_at   | TIMESTAMP | Creation timestamp
expires_at   | TIMESTAMP | Expiration timestamp (nullable)
is_active    | BOOLEAN   | Soft delete flag
```

**click_events** (Analytics)
```sql
Column       | Type      | Description
-------------|-----------|----------------------------------
id           | BIGSERIAL | Primary key
short_code   | VARCHAR   | Reference to url_mappings
clicked_at   | TIMESTAMP | Click timestamp
ip_address   | VARCHAR   | Client IP address
user_agent   | TEXT      | Browser/client info
referrer     | TEXT      | HTTP referrer
```

## 🧪 Testing

### Run Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=UrlServiceTest

# Integration tests only
mvn verify
```

### Test Coverage

```bash
# Generate coverage report
mvn jacoco:report

# View report
open target/site/jacoco/index.html
```

### Manual Testing

```bash
# Create URL
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.example.com"}'

# Test redirect
curl -L http://localhost:8080/{shortCode}

# Check Redis cache
docker exec -it urlshortener-redis redis-cli
> KEYS *
> GET short:{shortCode}

# Check database
docker exec -it urlshortener-postgres psql -U dbuser -d urlshortener
> SELECT * FROM url_mappings;
> SELECT * FROM click_events;
```

## 🚢 Deployment

### Docker Production Build

```bash
# Build optimized image
docker build -t url-shortener:1.0.0 .

# Run in production mode
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/urlshortener \
  -e SPRING_DATA_REDIS_HOST=redis-host \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092 \
  url-shortener:1.0.0
```

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/urlshortener
SPRING_DATASOURCE_USERNAME=dbuser
SPRING_DATASOURCE_PASSWORD=dbpass

# Redis
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Application
SERVER_PORT=8080
```

### Scaling Horizontally

```yaml
# docker-compose scale example
docker-compose up -d --scale app=3

# Or with Kubernetes
kubectl scale deployment url-shortener --replicas=5
```

**Requirements for Horizontal Scaling:**
- ✅ Stateless application (no session storage)
- ✅ Shared Redis cache
- ✅ Shared PostgreSQL database
- ✅ Load balancer in front

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Coding Standards

- Follow Java naming conventions
- Add unit tests for new features
- Update documentation for API changes
- Run `mvn clean verify` before committing

## 📞 Contact

**Email:** sparshgirdhar19@gmail.com  
**LinkedIn:** [Sparsh Girdhar](https://www.linkedin.com/in/sparsh-girdhar-979a60163/)

## 🗺️ Roadmap

### Completed ✅
- [x] URL shortening with Base62 encoding
- [x] Redis caching with cache-aside pattern
- [x] Kafka-based async analytics
- [x] Rate limiting (IP-based)
- [x] Read-time expiry validation
- [x] Idempotency support
- [x] Docker containerization
- [x] Database migrations with Flyway

### Future Enhancements 🚀
- [ ] Analytics dashboard (React frontend)
- [ ] Custom short codes (vanity URLs)
- [ ] QR code generation
- [ ] User authentication & API keys
- [ ] Multi-tenancy support
- [ ] Geo-location analytics
- [ ] Click fraud detection
- [ ] Kubernetes deployment manifests
- [ ] Prometheus metrics export
- [ ] GraphQL API

<div align="center">

**⭐ Star this repo if you find it helpful!**

Made with ❤️ by SPARSH

</div>
