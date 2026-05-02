# FounderLink

FounderLink is a completed full-stack microservices platform for connecting founders, investors, co-founders, and admins in a startup ecosystem.

The system includes an Angular web application, Spring Boot microservices, service discovery, centralized configuration, PostgreSQL databases, Redis-backed token invalidation, RabbitMQ event messaging, in-app and email notifications, Swagger documentation, Docker Compose orchestration, SonarQube configuration, and GitHub Actions CI/CD.

## Table of Contents

- [Project Status](#project-status)
- [Core Features](#core-features)
- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Environment Variables](#environment-variables)
- [Useful URLs](#useful-urls)
- [API Overview](#api-overview)
- [Running Tests](#running-tests)
- [CI/CD](#cicd)
- [SonarQube](#sonarqube)
- [Project Structure](#project-structure)
- [Database Layout](#database-layout)

## Project Status

This project is complete and ready for local demo, evaluation, and container-based deployment.

Completed modules:

- Role-based authentication and authorization.
- Founder, investor, co-founder, and admin workflows.
- Startup creation, discovery, approval, updates, documents, and follow/unfollow.
- Team invite and team member management.
- Investment request and founder approval/rejection flow.
- Messaging between investors/users and startup founders.
- Event-driven notification handling through RabbitMQ.
- Angular frontend with protected routes, role-aware navigation, dashboards, forms, and data views.
- Docker Compose environment for the complete platform.
- Backend unit/controller tests and frontend build pipeline.
- GitHub Actions workflow for testing, building, and publishing Docker images.

## Core Features

### Founder

- Register, verify email, log in, and manage profile.
- Create and edit startup profiles.
- Manage startup details, social links, updates, and documents.
- Invite and manage team members.
- View investment requests received for owned startups.
- Approve or reject investment requests.
- Reply to startup-related messages.

### Investor

- Discover and filter startups.
- View startup details.
- Follow startups.
- Submit investment requests.
- Track own investment activity.
- Message startup founders.

### Co-Founder / Team Member

- Accept or reject team invites.
- Access team/startup collaboration areas according to assigned role.
- Participate in startup management workflows where permitted.

### Admin

- Review pending startups.
- Approve, reject, or manage startup status.
- View platform users.
- Change user roles.
- Review investment activity.

## Architecture

```text
Angular Frontend
    |
    v
API Gateway
    |
    +--> Auth Service
    +--> User Service
    +--> Startup Service
    +--> Team Service
    +--> Investment Service
    +--> Messaging Service
    +--> Notification Service

Supporting services:
    Config Server
    Eureka Server
    PostgreSQL
    Redis
    RabbitMQ
    Zipkin
    SonarQube
```

Request flow:

```text
Client
  -> API Gateway
      validates JWT
      checks Redis token blacklist
      injects trusted X-User-Email and X-User-Role headers
      routes to domain services
  -> Domain Service
      enforces business rules
      persists data in its own database
      publishes events when needed
  -> RabbitMQ
      delivers domain events
  -> Notification Service
      creates notifications and sends email when enabled
```

Each backend service is independently deployable and owns its own database. Services communicate through the gateway, Feign clients, and RabbitMQ events.

## Services

| Service | Responsibility | Internal Port | Host Port |
| --- | --- | ---: | ---: |
| `config-server` | Centralized runtime configuration | `8888` | `18888` |
| `eureka-server` | Service discovery and registry | `8761` | `18761` |
| `api-gateway` | Gateway routing, JWT validation, Redis blacklist check | `8083` | `19080` |
| `auth-service` | Register, login, OTP verification, refresh tokens, logout | `8089` | via gateway |
| `user-service` | User profiles, preferences, admin role management | `9000` | via gateway |
| `startup-service` | Startup CRUD, search, status lifecycle, follows, updates, documents | `8084` | via gateway |
| `team-service` | Team invites, membership, roles, invite status | `8085` | via gateway |
| `investment-service` | Investment requests and founder decisions | `8088` | via gateway |
| `messaging-service` | Conversations, inbox, startup messages, founder replies | `8087` | via gateway |
| `notification-service` | In-app notifications and email event consumers | `8086` | via gateway |
| `founderlink-new-frontend` | Angular web application | `80` in Docker / `4200` local dev | `14200` Docker / `4200` local dev |

## Tech Stack

### Frontend

- Angular 17
- TypeScript
- Angular Router with protected routes
- PrimeNG and PrimeIcons
- Tailwind CSS
- ECharts / ngx-echarts
- RxJS

### Backend

- Java 21
- Spring Boot 3
- Spring Cloud Gateway
- Spring Cloud Config
- Spring Cloud Netflix Eureka
- Spring Security
- Spring Data JPA
- Spring AMQP
- OpenFeign
- Flyway
- Springdoc OpenAPI / Swagger UI
- JUnit 5 and Mockito

### Infrastructure

- PostgreSQL 16
- Redis 7
- RabbitMQ 3 with management UI
- Zipkin
- SonarQube Community Edition
- Docker and Docker Compose
- GitHub Actions
- GitHub Container Registry

## Quick Start

### Prerequisites

- Java 21
- Maven or the included Maven wrappers
- Node.js 20 and npm
- Docker Desktop
- Git

### 1. Clone the repository

```bash
git clone <repository-url>
cd founderlink
```

### 2. Configure environment variables

Copy the Docker environment template:

```bash
cp .env.docker.example .env
```

On Windows PowerShell:

```powershell
Copy-Item .env.docker.example .env
```

Update `.env` with secure values:

```env
POSTGRES_PASSWORD=change-me
RABBITMQ_USERNAME=change-me
RABBITMQ_PASSWORD=change-me
JWT_SECRET=change-me-to-a-long-random-secret
```

`JWT_SECRET` must be a long random value suitable for HMAC-SHA256 signing.

### 3. Start the complete stack

```bash
docker compose up -d --build
```

The first startup can take a few minutes because Docker builds every service image and waits for health checks.

### 4. Open the application

If running the frontend locally:

```bash
cd founderlink-new-frontend
npm install
npm start
```

Open the Dockerized frontend:

```text
http://localhost:14200
```

For local frontend development:

```text
http://localhost:4200
```

Backend API traffic goes through:

```text
http://localhost:19080
```

### 5. Stop the stack

```bash
docker compose down
```

To remove volumes as well:

```bash
docker compose down -v
```

## Environment Variables

The main variables are defined in `.env.docker.example`.

| Variable | Default | Description |
| --- | --- | --- |
| `POSTGRES_USER` | `postgres` | PostgreSQL superuser |
| `POSTGRES_PASSWORD` | required | PostgreSQL password |
| `RABBITMQ_USERNAME` | required | RabbitMQ username |
| `RABBITMQ_PASSWORD` | required | RabbitMQ password |
| `JWT_SECRET` | required | JWT signing secret |
| `POSTGRES_HOST_PORT` | `15432` | PostgreSQL host port |
| `REDIS_HOST_PORT` | `16379` | Redis host port |
| `RABBITMQ_HOST_PORT` | `15673` | RabbitMQ AMQP host port |
| `RABBITMQ_MANAGEMENT_HOST_PORT` | `15672` | RabbitMQ dashboard port |
| `FRONTEND_HOST_PORT` | `14200` | Dockerized frontend host port |
| `ZIPKIN_HOST_PORT` | `19411` | Zipkin dashboard port |
| `CONFIG_SERVER_HOST_PORT` | `18888` | Config server host port |
| `EUREKA_HOST_PORT` | `18761` | Eureka dashboard port |
| `API_GATEWAY_HOST_PORT` | `19080` | API gateway host port |
| `SONARQUBE_HOST_PORT` | `19000` | SonarQube dashboard port |
| `MAIL_ENABLED` | `false` | Enables real email delivery |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | empty | SMTP username |
| `MAIL_PASSWORD` | empty | SMTP password |
| `OTP_EXPIRY_MINUTES` | `10` | OTP validity window |

## Useful URLs

| Tool / App | URL |
| --- | --- |
| Angular frontend, Docker | `http://localhost:14200` |
| Angular frontend, local dev | `http://localhost:4200` |
| API Gateway | `http://localhost:19080` |
| Swagger UI | `http://localhost:19080/swagger-ui.html` |
| Eureka dashboard | `http://localhost:18761` |
| RabbitMQ dashboard | `http://localhost:15672` |
| Zipkin dashboard | `http://localhost:19411` |
| SonarQube dashboard | `http://localhost:19000` |
| PostgreSQL | `localhost:15432` |
| Redis | `localhost:16379` |

## API Overview

All backend requests should go through the API Gateway:

```text
http://localhost:19080
```

Protected routes require:

```http
Authorization: Bearer <access_token>
```

### Authentication

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/auth/register` | Register a user |
| `POST` | `/auth/login` | Log in and receive access/refresh tokens |
| `POST` | `/auth/verify-otp` | Verify registration OTP |
| `POST` | `/auth/resend-otp` | Resend OTP |
| `POST` | `/auth/refresh` | Refresh access token |
| `POST` | `/auth/logout` | Blacklist current access token |
| `POST` | `/auth/forgot-password` | Start password reset |
| `POST` | `/auth/reset-password` | Reset password |
| `POST` | `/auth/change-password` | Change password for logged-in user |

Example login:

```bash
curl -X POST http://localhost:19080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"founder@example.com","password":"secret123"}'
```

Supported roles:

```text
ROLE_FOUNDER
ROLE_INVESTOR
ROLE_COFOUNDER
ROLE_ADMIN
```

### Users

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/users/{email}` | Get profile by email |
| `POST` | `/users` | Create user profile, normally called by auth-service |
| `GET` | `/users` | Admin: list users |
| `PUT` | `/users/{email}` | Update profile |
| `GET` | `/users/{email}/preferences` | Get user preferences |
| `PUT` | `/users/{email}/preferences` | Update user preferences |
| `PUT` | `/users/{email}/role` | Admin: update user role |

### Startups

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/startups` | Founder: create startup |
| `GET` | `/startups` | List startups |
| `GET` | `/startups/search` | Search/filter startups |
| `GET` | `/startups/{id}` | View startup details |
| `PUT` | `/startups/{id}` | Founder: update startup |
| `DELETE` | `/startups/{id}` | Founder/Admin: delete startup |
| `PUT` | `/startups/{id}/approve` | Admin: approve startup |
| `PUT` | `/startups/{id}/reject` | Admin: reject startup |
| `POST` | `/startups/{id}/follow` | Follow startup |
| `DELETE` | `/startups/{id}/unfollow` | Unfollow startup |
| `GET` | `/startups/{id}/followers` | View followers |
| `POST` | `/startups/{id}/updates` | Create startup update |
| `GET` | `/startups/{id}/updates` | List startup updates |
| `POST` | `/startups/{id}/documents` | Add document metadata |
| `POST` | `/startups/{id}/documents/upload` | Upload startup document |
| `GET` | `/startups/{id}/documents` | List startup documents |
| `GET` | `/startups/{id}/documents/{documentId}/download` | Download startup document |
| `DELETE` | `/startups/{id}/documents/{documentId}` | Delete startup document |

### Team

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/team/invite` | Founder: invite team member |
| `PUT` | `/team/invite/{id}/status` | Accept or reject invite |
| `GET` | `/team/startup/{startupId}` | Get startup team |
| `GET` | `/team/my` | Get current user's invites/memberships |
| `DELETE` | `/team/{id}` | Remove team member |

### Investments

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/investments` | Investor: submit investment request |
| `GET` | `/investments/me` | View own investments |
| `GET` | `/investments` | Admin: view all investments |
| `GET` | `/investments/startup/{id}` | Founder: view startup investment requests |
| `PUT` | `/investments/{id}/status` | Founder/Admin: update investment status |

### Messaging

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/messages` | Send a message |
| `GET` | `/messages/conversation/{id}` | Get conversation messages |
| `GET` | `/messages/startup/{id}` | Founder: get startup conversations |
| `GET` | `/messages/me` | Get current user's conversations |

### Notifications

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/notifications` | Get current user's notifications |
| `PUT` | `/notifications/{id}/read` | Mark one notification as read |
| `PUT` | `/notifications/read-all` | Mark all notifications as read |

## Running Tests

Each Spring Boot service has its own test suite.

Run one service:

```bash
cd auth-service
./mvnw test
```

Windows PowerShell:

```powershell
cd auth-service
.\mvnw.cmd test
```

Run all backend tests from the repository root:

```powershell
@("api-gateway","auth-service","config-server","eureka-server",
  "investment-service","messaging-service","notification-service",
  "startup-service","team-service","user-service") | ForEach-Object {
    Write-Host "Testing $_..."
    mvn -q -s .\maven-settings.xml -f ".\$_\pom.xml" test
}
```

Run frontend build:

```bash
cd founderlink-new-frontend
npm install
npm run build
```

Run frontend tests:

```bash
cd founderlink-new-frontend
npm test
```

Backend coverage reports are generated under:

```text
<service>/target/site/jacoco/index.html
```

## CI/CD

The repository includes a completed GitHub Actions workflow at:

```text
.github/workflows/ci-cd.yml
```

The workflow runs on pull requests and pushes to `main` or `master`.

It performs:

- Backend tests for each Spring Boot service.
- Frontend dependency installation and production build.
- Docker image packaging.
- Docker image publishing to GitHub Container Registry on push.

Published image pattern:

```text
ghcr.io/<github-username>/founderlink-api-gateway:latest
ghcr.io/<github-username>/founderlink-auth-service:latest
ghcr.io/<github-username>/founderlink-user-service:latest
ghcr.io/<github-username>/founderlink-startup-service:latest
ghcr.io/<github-username>/founderlink-team-service:latest
ghcr.io/<github-username>/founderlink-investment-service:latest
ghcr.io/<github-username>/founderlink-messaging-service:latest
ghcr.io/<github-username>/founderlink-notification-service:latest
ghcr.io/<github-username>/founderlink-config-server:latest
ghcr.io/<github-username>/founderlink-eureka-server:latest
ghcr.io/<github-username>/founderlink-frontend:latest
```

## SonarQube

Start SonarQube locally:

```bash
docker compose up -d postgres sonarqube
```

Open:

```text
http://localhost:19000
```

Default login:

```text
admin / admin
```

Build and test services before scanning:

```powershell
@("api-gateway","auth-service","config-server","eureka-server",
  "investment-service","messaging-service","notification-service",
  "startup-service","team-service","user-service") | ForEach-Object {
    mvn -q -s .\maven-settings.xml -f ".\$_\pom.xml" test
}
```

Run scanner with a generated token:

```powershell
$env:SONAR_TOKEN = "your-token"
sonar-scanner -Dsonar.host.url=http://localhost:19000 -Dsonar.token=$env:SONAR_TOKEN
```

The root `sonar-project.properties` file is configured for unified repository analysis.

## Project Structure

```text
founderlink/
|-- api-gateway/                  Spring Cloud Gateway, JWT filter, routing
|-- auth-service/                 Auth, OTP, refresh tokens, logout
|-- user-service/                 User profiles, preferences, admin users
|-- startup-service/              Startup lifecycle, search, follows, content
|-- team-service/                 Team invites and membership
|-- investment-service/           Investment request workflow
|-- messaging-service/            Conversations and replies
|-- notification-service/         Notifications and email event consumers
|-- config-server/                Spring Cloud Config Server
|-- eureka-server/                Eureka service registry
|-- founderlink-new-frontend/     Angular frontend application
|-- config-repo/                  Runtime configuration files
|-- docker/postgres/init/         PostgreSQL database initialization
|-- docs/study-pack/              Project explanation and evaluation notes
|-- .github/workflows/ci-cd.yml   CI/CD workflow
|-- docker-compose.yml            Full local environment
|-- .env.docker.example           Environment template
|-- sonar-project.properties      SonarQube configuration
|-- maven-settings.xml            Maven settings for local builds
`-- README.md                     Project documentation
```

## Database Layout

Each service owns its own PostgreSQL database. This avoids cross-service table coupling and keeps domain logic isolated.

| Database | Owner Service |
| --- | --- |
| `auth_db` | `auth-service` |
| `user_db` | `user-service` |
| `startup_db` | `startup-service` |
| `team_db` | `team-service` |
| `investment_db` | `investment-service` |
| `messaging_db` | `messaging-service` |
| `notification_db` | `notification-service` |
| `sonar_db` | `sonarqube` |

Flyway migrations live in each service under:

```text
src/main/resources/db/migration/
```

## Event-Driven Notifications

FounderLink uses RabbitMQ to decouple business workflows from notification delivery.

| Publisher | Exchange | Event / Routing Key | Consumer |
| --- | --- | --- | --- |
| `startup-service` | `startup.exchange` | `startup.created` | `notification-service` |
| `investment-service` | `investment.exchange` | `investment.created` | `notification-service` |
| `investment-service` | `investment.exchange` | `investment.status` | `notification-service` |
| `team-service` | `team.exchange` | `team.invite.sent` | `notification-service` |
| `team-service` | `team.exchange` | `team.invite.status` | `notification-service` |
| `messaging-service` | `messaging.exchange` | `message.reply.founder` | `notification-service` |

## Notes for Evaluation

- The frontend calls the API Gateway instead of calling individual services directly.
- Gateway authentication centralizes JWT validation and keeps downstream services behind trusted headers.
- Each backend service has its own persistence layer and Flyway migrations.
- RabbitMQ keeps notifications asynchronous and prevents notification delivery from blocking core business requests.
- Docker Compose starts the full system locally with reproducible service dependencies.
- GitHub Actions validates the project and publishes deployable container images.

---

Built with Java 21, Spring Boot 3, Angular 17, PostgreSQL, Redis, RabbitMQ, Docker, and GitHub Actions.
