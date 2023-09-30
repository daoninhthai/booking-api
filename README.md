# Booking API

A comprehensive booking and reservation system built with Spring Boot 3.0, Java 17, and PostgreSQL.

## Features

- **User Authentication**: JWT-based authentication with role-based access control (Customer, Provider, Admin)
- **Service Catalog**: Full CRUD operations for managing service offerings with categories, pricing, and search
- **Provider Management**: Provider registration, profile management, and service association
- **Booking System**: Complete booking lifecycle (create, confirm, cancel, complete) with conflict detection
- **Time Slot Management**: Flexible availability management with day-of-week scheduling
- **Review & Rating System**: Post-booking reviews with 1-5 star ratings and automatic provider rating calculation
- **Payment Integration**: Payment processing with support for Card, Cash, and Transfer methods
- **Recurring Bookings**: Weekly, bi-weekly, and monthly recurring booking support with auto-generation
- **Search & Filtering**: Advanced search with JPA Specifications for services and providers
- **Dashboard Analytics**: Provider analytics including revenue tracking, booking stats, and customer retention
- **Waitlist System**: Automatic waitlist management with notification and auto-booking
- **Calendar Export**: iCal (.ics) export for bookings and provider schedules
- **Promotions**: Discount system with percentage and fixed-amount promotions
- **Real-time Notifications**: WebSocket (STOMP) for real-time booking updates
- **Email Notifications**: Async booking confirmation, cancellation, and reminder emails
- **Redis Caching**: Availability data caching with configurable TTL
- **Multi-language Support**: i18n with English and Vietnamese translations
- **Prometheus Metrics**: Custom business metrics with Micrometer and Prometheus
- **Docker Support**: Multi-stage Dockerfile with Docker Compose for production and development

## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.0.2
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **ORM**: Spring Data JPA / Hibernate
- **Migration**: Flyway
- **Security**: Spring Security + JWT (jjwt 0.12.3)
- **Build**: Maven
- **Monitoring**: Spring Actuator + Micrometer + Prometheus
- **WebSocket**: Spring WebSocket (STOMP)
- **Containerization**: Docker + Docker Compose

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Services
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/services` | List all services (with optional filters) |
| GET | `/api/services/{id}` | Get service details |
| GET | `/api/services/popular` | Get popular services |
| POST | `/api/services` | Create service (Admin) |
| PUT | `/api/services/{id}` | Update service (Admin) |
| DELETE | `/api/services/{id}` | Deactivate service (Admin) |

### Providers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/providers` | List all providers |
| GET | `/api/providers/{id}` | Get provider details |
| POST | `/api/providers/register` | Register as provider |
| PUT | `/api/providers/{id}` | Update provider profile |

### Bookings
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Create a booking |
| GET | `/api/bookings/{id}` | Get booking details |
| GET | `/api/bookings/my-bookings` | Get my bookings |
| PUT | `/api/bookings/{id}/confirm` | Confirm booking (Provider) |
| PUT | `/api/bookings/{id}/cancel` | Cancel booking |
| PUT | `/api/bookings/{id}/complete` | Complete booking (Provider) |

### Availability
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/availability?providerId=&date=` | Get available slots |
| POST | `/api/availability/slots` | Create time slot (Provider) |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/reviews` | Create a review |
| GET | `/api/reviews/provider/{id}` | Get provider reviews |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments` | Process payment |
| POST | `/api/payments/{id}/refund` | Refund payment (Admin) |
| GET | `/api/payments/booking/{id}` | Get payment status |

### Search
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/search/services` | Search services |
| GET | `/api/search/providers` | Search providers |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats/{providerId}` | Booking statistics |
| GET | `/api/dashboard/revenue/{providerId}` | Revenue by period |
| GET | `/api/dashboard/top-services/{providerId}` | Top services |

### Calendar
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/calendar/export.ics` | Export bookings as iCal |

### Promotions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/promotions` | Create promotion (Admin) |
| GET | `/api/promotions/validate/{code}` | Validate promo code |
| POST | `/api/promotions/apply/{code}` | Apply promo code |

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+
- Docker & Docker Compose (optional)

### Quick Start with Docker

```bash
# Start all services
docker-compose up -d

# The API will be available at http://localhost:8080
```

### Development Setup

```bash
# Start infrastructure services only
docker-compose -f docker-compose.dev.yml up -d

# Run the application
mvn spring-boot:run
```

### Manual Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE booking_db;
```

2. Configure environment variables or update `application.yml`:
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export REDIS_HOST=localhost
export JWT_SECRET=your-secret-key
```

3. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

### Running Tests

```bash
mvn test
```

## Monitoring

- Health Check: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Prometheus: `GET /actuator/prometheus`

## Project Structure

```
src/main/java/com/daoninhthai/booking/
    config/          - Configuration classes
    controller/      - REST controllers
    dto/             - Request/Response DTOs
    entity/          - JPA entities
    enums/           - Enum types
    exception/       - Custom exceptions and handlers
    repository/      - JPA repositories
    security/        - JWT and Spring Security
    service/         - Business logic services
```

## License

This project is licensed under the MIT License.
