# FounderLink

> **A full-stack, microservices-based platform connecting startup founders with investors.**

FounderLink provides a structured ecosystem where founders can showcase their startups, manage teams, publish updates, and receive investment interest — while investors can discover opportunities, track portfolios, and communicate directly with founders.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Frontend](#frontend)
- [Infrastructure & DevOps](#infrastructure--devops)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Service Port Reference](#service-port-reference)
- [API Overview](#api-overview)
- [Known Gaps & Roadmap](#known-gaps--roadmap)

---

## Architecture Overview

FounderLink is built as a cloud-native microservices system following the Spring Cloud ecosystem patterns:

```
                        ┌─────────────────────┐
                        │   Angular Frontend   │
                        │   (Port 14200/80)    │
                        └──────────┬──────────┘
                                   │
                        ┌──────────▼──────────┐
                        │     API Gateway      │
                        │     (Port 8083)      │
                        └──────────┬──────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
 ┌────────▼───────┐    ┌───────────▼────────┐   ┌──────────▼────────┐
 │  Auth Service  │    │  Startup Service   │   │  User Service     │
 │  (Port 8089)   │    │  (Port 8084)       │   │  (Port 9000)      │
 └────────────────┘    └────────────────────┘   └───────────────────┘
          │                        │                        │
 ┌────────▼───────┐    ┌───────────▼────────┐   ┌──────────▼────────┐
 │  Investment    │    │   Team Service     │   │ Messaging Service  │
 │  (Port 8088)   │    │   (Port 8085)      │   │  (Port 8087)      │
 └────────────────┘    └────────────────────┘   └───────────────────┘
          │                        │                        │
 ┌────────▼───────────────────────▼────────────────────────▼────────┐
 │                     Notification Service (Port 8086)             │
 └──────────────────────────────────────────────────────────────────┘
          │                        │                        │
 ┌────────▼──────┐    ┌────────────▼──────┐   ┌────────────▼──────┐
 │   PostgreSQL  │    │     RabbitMQ      │   │      Redis        │
 └───────────────┘    └───────────────────┘   └───────────────────┘

         Config Server (8888) + Eureka Server (8761) underpin all services
```

All backend services register with **Eureka** for service discovery, pull configuration from a centralized **Spring Cloud Config Server**, and are routed through a single **API Gateway** that handles JWT validation and request routing. Distributed tracing is handled by **Zipkin**, and code quality is monitored via **SonarQube**.

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Configuration | Spring Cloud Config Server |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL 16 |
| Caching | Redis 7 |
| Messaging | RabbitMQ 3 |
| Distributed Tracing | Zipkin + Micrometer Brave |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Code Quality | SonarQube (Community Edition) |
| Build | Maven |

### Frontend
| Layer | Technology |
|---|---|
| Framework | Angular 17 |
| UI Library | PrimeNG 17 + PrimeIcons |
| Charting | Apache ECharts (ngx-echarts) |
| Styling | Tailwind CSS 3 |
| Language | TypeScript 5.4 |
| HTTP | Angular HttpClient |
| Web Server | Nginx (production) |

### DevOps
| Tool | Purpose |
|---|---|
| Docker + Docker Compose | Containerization & orchestration |
| Nginx | Frontend reverse proxy |
| GitHub | Source control |

---

## Microservices

### 1. Config Server (`config-server`) — Port 8888
Centralized configuration management using Spring Cloud Config. All services fetch their configuration from this server on startup. Supports native file-system backends. Services will not start until this service is healthy.

### 2. Eureka Server (`eureka-server`) — Port 8761
Service registry and discovery. All backend microservices register themselves here, allowing inter-service communication without hardcoded URLs. The dashboard is accessible at `http://localhost:18761`.

### 3. API Gateway (`api-gateway`) — Port 8083
Single entry point for all client requests. Responsible for:
- JWT validation and request filtering
- Intelligent routing to downstream services via Eureka
- Rate limiting using Redis
- Distributed tracing via Zipkin

### 4. Auth Service (`auth-service`) — Port 8089
Handles all authentication and authorization:
- User registration with OTP email verification
- JWT-based login and token refresh
- Forgot password / reset password via OTP
- Change password for authenticated users
- Role-based access control (`FOUNDER`, `INVESTOR`, `ADMIN`)
- Admin user seeding on startup
- Events published to RabbitMQ on user registration

**Key endpoints:**
| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and receive JWT |
| GET | `/auth/verify` | Email verification via link token |
| POST | `/auth/verify-otp` | Verify OTP code |
| POST | `/auth/resend-otp` | Resend OTP |
| POST | `/auth/forgot-password` | Request password reset OTP |
| POST | `/auth/reset-password` | Reset password with OTP |
| POST | `/auth/change-password` | Change password (authenticated) |
| POST | `/auth/refresh` | Refresh JWT token |
| POST | `/auth/logout` | Logout and invalidate token |

### 5. User Service (`user-service`) — Port 9000
Manages user profile data:
- Profile creation, retrieval, and update
- Avatar and document upload support
- Stores uploaded startup documents in a persistent volume

### 6. Startup Service (`startup-service`) — Port 8084
Core service for startup management:
- CRUD operations for startup profiles
- Startup discovery and search for investors
- Follow/unfollow startups
- Startup updates and announcements
- Admin approval workflow for pending startups
- Events published to RabbitMQ on startup actions

**Key endpoints:**
| Method | Path | Description |
|---|---|---|
| GET | `/startups` | Browse all approved startups |
| POST | `/startups` | Create a new startup (Founder) |
| GET | `/startups/{id}` | Get startup detail |
| PUT | `/startups/{id}` | Update startup |
| POST | `/startups/{id}/follow` | Follow a startup |
| GET | `/startups/my` | Get founder's own startups |

### 7. Investment Service (`investment-service`) — Port 8088
Tracks investment intents and transactions:
- Record investment interest in a startup
- Update investment status
- Investor portfolio view (`GET /investments/me`)
- Founder's received investments view (`GET /investments/startup/{id}`)
- Events published to RabbitMQ on investment actions

> ⚠️ **Note:** Payment gateway (Razorpay) integration is currently client-side only for demo purposes. See [Known Gaps](#known-gaps--roadmap).

### 8. Team Service (`team-service`) — Port 8085
Manages startup team composition:
- Founders can add, update, and remove team members from their startups
- Events published to RabbitMQ on team changes

### 9. Messaging Service (`messaging-service`) — Port 8087
Facilitates direct communication between founders and investors:
- Threaded conversations between two users
- Fetch all conversations for a user
- Fetch messages within a conversation
- Currently uses REST polling (WebSocket planned)

### 10. Notification Service (`notification-service`) — Port 8086
Delivers in-app notifications:
- Consumes events from RabbitMQ (investments, messages, startup actions)
- Stores notifications per user
- Mark notifications as read
- Currently uses REST polling (WebSocket planned)

---

## Frontend

The Angular 17 frontend is a role-aware single-page application with dedicated dashboards and feature sets per user role.

### Features by Role

**Founders:**
- Dashboard with portfolio overview and charts (ECharts)
- Create and manage startup listings
- Manage team members
- Publish startup updates
- View investment interest received
- Upload and manage startup documents

**Investors:**
- Dashboard with investment summary
- Browse and search startup listings
- View detailed startup profiles
- Express investment interest (with Razorpay demo integration)
- Follow startups
- Track personal investment portfolio

**Admin:**
- Approve or reject pending startup submissions
- User management panel (stub — backend API pending)
- Platform-wide investment ledger (stub — backend API pending)

**All Roles:**
- Real-time-style notification bell (polls every 30s)
- In-app messaging inbox (polls every 15s when a chat is open)
- User profile management
- Secure login, registration, OTP verification, password reset

### Key UI Libraries
- **PrimeNG** — Comprehensive UI component library (tables, dialogs, forms, toasts)
- **Apache ECharts (ngx-echarts)** — Data visualization for dashboards
- **Tailwind CSS** — Utility-first styling

---

## Infrastructure & DevOps

### Docker Compose

The entire stack — including all backing services and microservices — is orchestrated via a single `docker-compose.yml`. Services start in dependency order with health checks at every layer.

**Infrastructure services:**
- `postgres` — Shared PostgreSQL instance; each microservice uses its own database schema
- `redis` — Used by the API Gateway for rate limiting and by Auth Service for session/OTP storage
- `rabbitmq` — Async event bus between services; management UI at port `15672`
- `zipkin` — Distributed tracing UI at port `19411`
- `sonarqube` — Code quality dashboard at port `19000`

### SonarQube

The project is pre-configured for SonarQube analysis via `sonar-project.properties`, with source and test paths defined for all 10 microservices, JaCoCo coverage reports, and exclusions for generated/infrastructure code.

---

## Getting Started

### Prerequisites

- **Docker** 24+ and **Docker Compose** v2
- Ports listed in [Service Port Reference](#service-port-reference) must be available

### 1. Clone the repository

```bash
git clone https://github.com/ch-abhiram/founderlink.git
cd founderlink
```

### 2. Configure environment variables

Create a `.env` file in the project root:

```env
# PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_strong_password

# RabbitMQ
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# JWT
JWT_SECRET=your_256bit_or_longer_secret_key_here

# Mail (optional — set MAIL_ENABLED=false to skip)
MAIL_ENABLED=false
MAIL_FROM=no-reply@founderlink.local
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# OTP expiry (minutes)
OTP_EXPIRY_MINUTES=10
```

### 3. Start the stack

```bash
docker compose up --build
```

> Services start in order: PostgreSQL → Redis → RabbitMQ → Config Server → Eureka → API Gateway → all microservices → Frontend. Allow 3–5 minutes for full initialization on first boot.

### 4. Access the application

| Service | URL |
|---|---|
| **Frontend** | http://localhost:14200 |
| **API Gateway** | http://localhost:19080 |
| **Eureka Dashboard** | http://localhost:18761 |
| **RabbitMQ Management** | http://localhost:15672 |
| **Zipkin Tracing** | http://localhost:19411 |
| **SonarQube** | http://localhost:19000 |

### 5. Default Admin Account

An admin user is seeded automatically on first startup by `AdminUserSeeder` in the Auth Service. Check the auth-service configuration or logs for the default credentials, and change the password immediately after first login.

### Local Development (without Docker)

To run a single service locally:

```bash
cd auth-service
./mvnw spring-boot:run
```

Ensure the Config Server and Eureka Server are running first (or override their URLs via environment variables), and that PostgreSQL, Redis, and RabbitMQ are accessible.

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `POSTGRES_USER` | `postgres` | PostgreSQL username |
| `POSTGRES_PASSWORD` | *(required)* | PostgreSQL password |
| `POSTGRES_HOST_PORT` | `15432` | Host port for PostgreSQL |
| `RABBITMQ_USERNAME` | *(required)* | RabbitMQ username |
| `RABBITMQ_PASSWORD` | *(required)* | RabbitMQ password |
| `RABBITMQ_HOST_PORT` | `15673` | Host port for RabbitMQ AMQP |
| `RABBITMQ_MANAGEMENT_HOST_PORT` | `15672` | Host port for RabbitMQ Management UI |
| `JWT_SECRET` | *(required)* | Secret for JWT signing (min. 256-bit) |
| `MAIL_ENABLED` | `false` | Enable email sending |
| `MAIL_FROM` | `no-reply@founderlink.local` | Sender email address |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | *(empty)* | SMTP username |
| `MAIL_PASSWORD` | *(empty)* | SMTP password |
| `OTP_EXPIRY_MINUTES` | `10` | OTP validity window |
| `REDIS_HOST_PORT` | `16379` | Host port for Redis |
| `API_GATEWAY_HOST_PORT` | `19080` | Host port for API Gateway |
| `EUREKA_HOST_PORT` | `18761` | Host port for Eureka Server |
| `CONFIG_SERVER_HOST_PORT` | `18888` | Host port for Config Server |
| `ZIPKIN_HOST_PORT` | `19411` | Host port for Zipkin |
| `SONARQUBE_HOST_PORT` | `19000` | Host port for SonarQube |
| `FRONTEND_HOST_PORT` | `14200` | Host port for the Angular frontend |

---

## Service Port Reference

| Service | Internal Port | Default Host Port |
|---|---|---|
| Frontend (Nginx) | 80 | 14200 |
| API Gateway | 8083 | 19080 |
| Config Server | 8888 | 18888 |
| Eureka Server | 8761 | 18761 |
| Auth Service | 8089 | — |
| Startup Service | 8084 | — |
| Team Service | 8085 | — |
| Notification Service | 8086 | — |
| Messaging Service | 8087 | — |
| Investment Service | 8088 | — |
| User Service | 9000 | — |
| PostgreSQL | 5432 | 15432 |
| Redis | 6379 | 16379 |
| RabbitMQ (AMQP) | 5672 | 15673 |
| RabbitMQ (Management) | 15672 | 15672 |
| Zipkin | 9411 | 19411 |
| SonarQube | 9000 | 19000 |

> Backend services communicate internally via the Docker network and are not individually exposed on the host by default.

---

## API Overview

All API routes pass through the API Gateway at `http://localhost:19080`. JWT Bearer token is required for all protected routes.

```
/api/auth/**          → auth-service
/api/users/**         → user-service
/api/startups/**      → startup-service
/api/investments/**   → investment-service
/api/teams/**         → team-service
/api/messages/**      → messaging-service
/api/notifications/** → notification-service
```

Swagger UI is available on each service individually (when running locally) at:
```
http://localhost:{SERVICE_PORT}/swagger-ui.html
```

---

## Known Gaps & Roadmap

The following features have frontend implementations but are awaiting backend support. See [`BACKEND_GAPS.md`](./BACKEND_GAPS.md) for full details.

| Gap | Status | Frontend Mitigation |
|---|---|---|
| **WebSocket / Real-time messaging** | Backend REST only | Polling every 15s (inbox) / 30s (notifications) |
| **Admin: Get all users** | Not implemented | Placeholder UI with explanation |
| **Admin: Platform-wide investment ledger** | Not implemented | Placeholder UI with explanation |
| **Razorpay server-side order & verification** | Client-side only (demo) | Direct Razorpay SDK call; insecure for production |
| **Unfollow a startup** (`DELETE /startups/{id}/follow`) | Not implemented | Calls pseudo-endpoint; returns 404 |

### Planned Backend Improvements

1. Implement Spring WebSocket (STOMP) in `messaging-service` and `notification-service` for real-time push
2. Integrate `razorpay-java` SDK for server-side order creation and signature verification
3. Add Admin Controller routes in `auth-service` and `investment-service` with `@PreAuthorize("hasRole('ROLE_ADMIN')")`
4. Implement `DELETE /startups/{id}/follow` for the unfollow flow

---

## Project Structure

```
founderlink/
├── api-gateway/              # Spring Cloud Gateway
├── auth-service/             # Authentication & authorization
├── config-server/            # Centralized configuration
├── eureka-server/            # Service registry
├── investment-service/       # Investment tracking
├── messaging-service/        # Direct messaging
├── notification-service/     # In-app notifications
├── startup-service/          # Startup listings
├── team-service/             # Team management
├── user-service/             # User profiles
├── founderlink-new-frontend/ # Angular 17 SPA
│   └── src/app/
│       ├── features/         # Feature modules (auth, founder, investor, admin…)
│       ├── shared/           # Shared components, pipes
│       └── layouts/          # App shell and auth layouts
├── docker/
│   └── postgres/init/        # Database initialization scripts
├── docker-compose.yml        # Full-stack orchestration
├── sonar-project.properties  # SonarQube configuration
└── BACKEND_GAPS.md           # Known missing backend APIs
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes with clear messages
4. Push and open a Pull Request

Please ensure all new backend code includes unit tests and that SonarQube quality gates pass before submitting a PR.

---

## License

This project is currently unlicensed. All rights reserved by the author.
