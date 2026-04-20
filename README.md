# FounderLink

**A microservices backend platform connecting founders, investors, and startup teams.**

FounderLink is built with Java 21, Spring Boot 3, and Spring Cloud. It handles the full lifecycle of a startup ecosystem — authentication, startup creation, team collaboration, investment tracking, direct messaging, and event-driven notifications — all through independently deployable services behind a single API gateway.

---

## Table of Contents

- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [API Reference](#api-reference)
- [Quick Start](#quick-start)
- [Environment Variables](#environment-variables)
- [Running Tests](#running-tests)
- [SonarQube](#sonarqube)
- [Project Structure](#project-structure)

---

## Architecture

```
                         ┌─────────────────────────────┐
                         │         Config Server         │
                         │           :8888               │
                         └──────────────┬──────────────┘
                                        │ config on startup
          ┌─────────────────────────────▼─────────────────────────────┐
          │                      Eureka Server                         │
          │                         :8761                              │
          └──────────┬──────────────────────────┬─────────────────────┘
                     │ registers                 │ discovers
          ┌──────────▼──────────────────────────▼─────────────────────┐
          │                       API Gateway                          │
          │           :8083  —  JWT validation + routing               │
          └──┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬───────┘
             │      │      │      │      │      │      │      │
           auth   user  startup  team invest  msg  notify   (more)
          :8089  :9000  :8084  :8085  :8088  :8087  :8086

Each service owns its own PostgreSQL database. Async events flow
through RabbitMQ. Redis backs the gateway's JWT blacklist.
```

### Request flow

```
Client
  └─► API Gateway          validates JWT, injects X-User-Email / X-User-Role headers
        └─► Domain Service  trusts gateway headers, enforces business rules
              └─► PostgreSQL (own schema)
              └─► RabbitMQ  (publishes domain events)
                    └─► Notification Service  (consumes events, sends emails)
```

### Event bus

| Publisher         | Exchange              | Routing key              | Consumer             |
|-------------------|-----------------------|--------------------------|----------------------|
| auth-service      | `user.events`         | —                        | (internal log)       |
| startup-service   | `startup.exchange`    | `startup.created`        | notification-service |
| investment-service| `investment.exchange` | `investment.created`     | notification-service |
| investment-service| `investment.exchange` | `investment.status`      | notification-service |
| team-service      | `team.exchange`       | `team.invite.sent`       | notification-service |
| team-service      | `team.exchange`       | `team.invite.status`     | notification-service |
| messaging-service | `messaging.exchange`  | `message.reply.founder`  | notification-service |

---

## Services

| Service              | Responsibility                                      | Port   |
|----------------------|-----------------------------------------------------|--------|
| `config-server`      | Centralised configuration for all services          | `8888` |
| `eureka-server`      | Service discovery and registry                      | `8761` |
| `api-gateway`        | JWT validation, routing, Redis blacklist check      | `8083` |
| `auth-service`       | Register, login, email verification, token refresh  | `8089` |
| `user-service`       | User profiles, skills, portfolio, role management   | `9000` |
| `startup-service`    | Startup CRUD, search, follow, status lifecycle      | `8084` |
| `team-service`       | Team invites, membership, invite status updates     | `8085` |
| `investment-service` | Investment requests, founder approval/rejection     | `8088` |
| `messaging-service`  | Startup conversations, inbox, founder replies       | `8087` |
| `notification-service`| In-app notifications, email delivery via RabbitMQ  | `8086` |

### Supporting infrastructure

| Component   | Purpose                                         |
|-------------|-------------------------------------------------|
| PostgreSQL   | One database per domain (`auth_db`, `user_db`, etc.) |
| Redis        | JWT blacklist for logout invalidation           |
| RabbitMQ     | Async event delivery between services           |
| Zipkin       | Distributed tracing across service calls        |
| SonarQube    | Code quality, coverage, and security scanning   |

---

## Tech Stack

**Language & Runtime**
- Java 21
- Maven (per-service wrappers included)

**Core Frameworks**
- Spring Boot 3
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Security
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- OpenFeign (inter-service HTTP calls)

**Resilience & Observability**
- Resilience4j (circuit breaker on user-service calls)
- Flyway (database migrations)
- Springdoc OpenAPI / Swagger UI (aggregated via gateway)
- Micrometer + Zipkin (distributed tracing)
- JaCoCo (test coverage)

**Infrastructure**
- PostgreSQL 16
- Redis 7
- RabbitMQ 3
- Docker + Docker Compose
- SonarQube (Community Edition)

---

## API Reference

All requests go through the gateway at `http://localhost:19080`. Protected routes require:
```
Authorization: Bearer <access_token>
```

### Auth — `/auth`

| Method | Path              | Auth | Description                                 |
|--------|-------------------|------|---------------------------------------------|
| POST   | `/auth/register`  | —    | Register a new user (triggers email verify) |
| POST   | `/auth/login`     | —    | Login, returns access + refresh tokens      |
| GET    | `/auth/verify`    | —    | Verify email via `?token=` query param      |
| POST   | `/auth/refresh`   | —    | Exchange refresh token for new access token |
| POST   | `/auth/logout`    | ✓    | Blacklists current access token in Redis    |

**Register request body**
```json
{
  "email": "jane@example.com",
  "password": "secret123",
  "role": "ROLE_FOUNDER"
}
```
Valid roles: `ROLE_FOUNDER`, `ROLE_INVESTOR`, `ROLE_COFOUNDER`, `ROLE_ADMIN`

**Login response**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "uuid-v4",
  "email": "jane@example.com",
  "role": "ROLE_FOUNDER"
}
```

---

### Users — `/users`

| Method | Path                    | Auth  | Description                    |
|--------|-------------------------|-------|--------------------------------|
| GET    | `/users/me`             | ✓     | Get own profile                |
| GET    | `/users/{email}`        | ✓     | Get profile by email           |
| PUT    | `/users/{email}`        | ✓     | Update profile (name, bio etc) |
| GET    | `/admin/users`          | ADMIN | List all users                 |
| PUT    | `/admin/users/{email}/role` | ADMIN | Change a user's role       |

---

### Startups — `/startups`

| Method | Path                        | Auth    | Description                              |
|--------|-----------------------------|---------|------------------------------------------|
| POST   | `/startups`                 | FOUNDER | Create a startup                         |
| GET    | `/startups`                 | ✓       | List all (paginated)                     |
| GET    | `/startups/search`          | ✓       | Filter by `category`, `status`, `currentRound` |
| GET    | `/startups/{id}`            | ✓       | Get startup by ID                        |
| PUT    | `/startups/{id}`            | FOUNDER | Update own startup                       |
| DELETE | `/startups/{id}`            | FOUNDER/ADMIN | Delete startup                    |
| PUT    | `/startups/{id}/status`     | ADMIN   | Update status (`PENDING`/`OPEN`/`CLOSED`/`REJECTED`) |
| POST   | `/startups/{id}/follow`     | ✓       | Follow a startup                         |
| DELETE | `/startups/{id}/unfollow`   | ✓       | Unfollow a startup                       |
| GET    | `/startups/{id}/followers`  | ✓       | Get follower list                        |

---

### Team — `/team`

| Method | Path                        | Auth    | Description                         |
|--------|-----------------------------|---------|-------------------------------------|
| POST   | `/team/invite`              | FOUNDER | Invite a user to a startup's team   |
| PUT    | `/team/invite/{id}/status`  | ✓       | Accept or reject own invite         |
| GET    | `/team/startup/{startupId}` | ✓       | Get all members of a startup        |
| GET    | `/team/my`                  | ✓       | Get own invites / memberships       |
| DELETE | `/team/{id}`                | FOUNDER/MEMBER | Remove a team member         |

---

### Investments — `/investments`

| Method | Path                         | Auth     | Description                          |
|--------|------------------------------|----------|--------------------------------------|
| POST   | `/investments`               | INVESTOR | Submit an investment request         |
| GET    | `/investments/me`            | ✓        | Get own investments                  |
| GET    | `/investments/startup/{id}`  | FOUNDER  | View all investors in a startup      |
| PUT    | `/investments/{id}/approve`  | FOUNDER  | Approve an investment                |
| PUT    | `/investments/{id}/reject`   | FOUNDER  | Reject an investment                 |

---

### Messaging — `/messages`

| Method | Path                           | Auth | Description                               |
|--------|--------------------------------|------|-------------------------------------------|
| POST   | `/messages`                    | ✓    | Send a message (creates conversation if needed) |
| GET    | `/messages/conversation/{id}`  | ✓    | Get all messages in a conversation        |
| GET    | `/messages/startup/{id}`       | FOUNDER | All conversations for a startup        |
| GET    | `/messages/me`                 | ✓    | Own conversations (participant + sender)  |

Founders reply by including `participantEmail` in the request body.

---

### Notifications — `/notifications`

| Method | Path                      | Auth | Description                        |
|--------|---------------------------|------|------------------------------------|
| GET    | `/notifications`          | ✓    | Get own notifications              |
| PUT    | `/notifications/{id}/read`| ✓    | Mark a notification as read        |
| PUT    | `/notifications/read-all` | ✓    | Mark all notifications as read     |

Notifications are created automatically by domain events (new investment, team invite, etc.).

---

### Swagger UI

Aggregated documentation is available at the gateway:

```
http://localhost:19080/swagger-ui.html
```

Docs for each service are available individually at `/v3/api-docs` endpoints under their gateway prefix (e.g. `/auth/v3/api-docs`, `/startups/v3/api-docs`).

---

## Quick Start

### Prerequisites

- Docker Desktop (includes Docker Compose)
- Java 21 (only needed if building outside Docker)

### 1. Clone the repo

```bash
git clone https://github.com/ch-abhiram/founderlink.git
cd founderlink
```

### 2. Set up environment

```bash
cp .env.docker.example .env
```

Open `.env` and set at minimum:

```env
JWT_SECRET=your-long-random-secret-minimum-32-chars
POSTGRES_PASSWORD=your-db-password
RABBITMQ_USERNAME=your-rabbitmq-user
RABBITMQ_PASSWORD=your-rabbitmq-password
```

### 3. Start everything

```bash
docker compose up --build
```

This starts all services in dependency order:
1. PostgreSQL → creates all databases
2. Redis, RabbitMQ
3. Config Server → pulls `config-repo/` configuration
4. Eureka Server → service registry
5. API Gateway + all domain services

### 4. Verify

| URL                                      | What you should see            |
|------------------------------------------|--------------------------------|
| `http://localhost:18761`                 | Eureka dashboard               |
| `http://localhost:19080/swagger-ui.html` | Aggregated Swagger UI          |
| `http://localhost:15672`                 | RabbitMQ Management (guest/guest) |
| `http://localhost:19000`                 | SonarQube (admin/admin)        |

### 5. First API call

```bash
# Register
curl -X POST http://localhost:19080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"founder@example.com","password":"secret123","role":"ROLE_FOUNDER"}'

# Login
curl -X POST http://localhost:19080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"founder@example.com","password":"secret123"}'
```

> **Note on email verification:** Registration emails are logged to the auth-service console as verification tokens. Check the Docker logs and call `/auth/verify?token=<token>` before logging in.

---

## Environment Variables

All variables are defined in `.env` (from `.env.docker.example`).

| Variable                    | Default               | Description                            |
|-----------------------------|-----------------------|----------------------------------------|
| `JWT_SECRET`                | *(required)*          | HMAC-SHA256 secret — minimum 32 chars  |
| `POSTGRES_USER`             | `postgres`            | PostgreSQL superuser                   |
| `POSTGRES_PASSWORD`         | *(required)*          | PostgreSQL superuser password          |
| `RABBITMQ_USERNAME`         | *(required)*          | RabbitMQ admin username                |
| `RABBITMQ_PASSWORD`         | *(required)*          | RabbitMQ admin password                |
| `POSTGRES_HOST_PORT`        | `15432`               | PostgreSQL exposed port on host        |
| `REDIS_HOST_PORT`           | `16379`               | Redis exposed port on host             |
| `RABBITMQ_HOST_PORT`        | `15673`               | RabbitMQ AMQP port on host             |
| `RABBITMQ_MANAGEMENT_HOST_PORT` | `15672`           | RabbitMQ Management UI port on host    |
| `API_GATEWAY_HOST_PORT`     | `19080`               | Gateway exposed port on host           |
| `EUREKA_HOST_PORT`          | `18761`               | Eureka dashboard port on host          |
| `CONFIG_SERVER_HOST_PORT`   | `18888`               | Config server port on host             |
| `SONARQUBE_HOST_PORT`       | `19000`               | SonarQube UI port on host              |
| `ZIPKIN_HOST_PORT`          | `19411`               | Zipkin tracing port on host            |

Service-level environment variables (datasource URL, Eureka zone, Redis host, etc.) are resolved at runtime from the config server. See `config-repo/` for defaults.

---

## Running Tests

Each service has its own test suite with JUnit 5 and Mockito.

**Run tests for one service:**

```bash
# Linux / macOS
cd auth-service && ./mvnw test

# Windows
cd auth-service && .\mvnw.cmd test
```

**Run all services in sequence (Windows PowerShell):**

```powershell
@("api-gateway","auth-service","config-server","eureka-server",
  "investment-service","messaging-service","notification-service",
  "startup-service","team-service","user-service") | ForEach-Object {
    Write-Host "Testing $_..."
    mvn -q -s .\maven-settings.xml -f ".\$_\pom.xml" test
}
```

**Run all services in sequence (Linux / macOS):**

```bash
for svc in api-gateway auth-service config-server eureka-server \
           investment-service messaging-service notification-service \
           startup-service team-service user-service; do
  echo "Testing $svc..."
  cd $svc && ./mvnw -q test && cd ..
done
```

Coverage reports are generated per service at `<service>/target/site/jacoco/index.html`.

---

## SonarQube

The repo includes a `sonar-project.properties` at the root that scans all services as one project.

### Start SonarQube

```bash
docker compose up -d postgres sonarqube
```

Open `http://localhost:19000` and log in with `admin` / `admin`. On first login you will be prompted to change the password. Then generate a project token under **Account → Security**.

### Build all services first

SonarQube needs compiled bytecode and JaCoCo XML reports to give full analysis. Build before scanning:

```powershell
# Windows PowerShell
@("api-gateway","auth-service","config-server","eureka-server",
  "investment-service","messaging-service","notification-service",
  "startup-service","team-service","user-service") | ForEach-Object {
    mvn -q -s .\maven-settings.xml -f ".\$_\pom.xml" test
}
```

```bash
# Linux / macOS
for svc in api-gateway auth-service config-server eureka-server \
           investment-service messaging-service notification-service \
           startup-service team-service user-service; do
  cd $svc && ./mvnw -q test && cd ..
done
```

### Run the scan

**With a local sonar-scanner:**

```bash
# Linux / macOS
export SONAR_TOKEN=your-token
sonar-scanner -Dsonar.host.url=http://localhost:19000 -Dsonar.token=$SONAR_TOKEN
```

```powershell
# Windows PowerShell
$env:SONAR_TOKEN = "your-token"
sonar-scanner -Dsonar.host.url=http://localhost:19000 -Dsonar.token=$env:SONAR_TOKEN
```

**With Docker (no local install needed):**

```powershell
# Windows PowerShell
$env:SONAR_TOKEN = "your-token"
docker run --rm `
  -e SONAR_HOST_URL="http://host.docker.internal:19000" `
  -e SONAR_TOKEN="$env:SONAR_TOKEN" `
  -v "${PWD}:/usr/src" `
  sonarsource/sonar-scanner-cli
```

```bash
# Linux / macOS
export SONAR_TOKEN=your-token
docker run --rm \
  -e SONAR_HOST_URL="http://host.docker.internal:19000" \
  -e SONAR_TOKEN="$SONAR_TOKEN" \
  -v "$(pwd):/usr/src" \
  sonarsource/sonar-scanner-cli
```

Results are visible at `http://localhost:19000/projects`.

---

## Project Structure

```
founderlink/
│
├── api-gateway/                  # Spring Cloud Gateway — JWT filter, routing
├── auth-service/                 # Auth, refresh tokens, email verification
├── user-service/                 # User profiles and admin role management
├── startup-service/              # Startup lifecycle, search, follow
├── team-service/                 # Team invites and membership
├── investment-service/           # Investment requests and approval flow
├── messaging-service/            # Conversations and founder replies
├── notification-service/         # In-app notifications and email
│
├── config-server/                # Spring Cloud Config Server
├── eureka-server/                # Netflix Eureka — service registry
│
├── docker/
│   └── postgres/
│       └── init/
│           └── 01-create-databases.sql   # Creates all domain databases
│
├── docker-compose.yml            # Full local environment
├── .env.docker.example           # Template — copy to .env before starting
├── sonar-project.properties      # Unified SonarQube scan config
└── maven-settings.xml            # Maven settings for multi-service builds
```

### Database layout

Each service owns its own schema. No cross-database joins — services communicate through APIs and events.

| Database          | Owner service       |
|-------------------|---------------------|
| `auth_db`         | auth-service        |
| `user_db`         | user-service        |
| `startup_db`      | startup-service     |
| `investment_db`   | investment-service  |
| `team_db`         | team-service        |
| `messaging_db`    | messaging-service   |
| `notification_db` | notification-service|

Schema migrations are managed by Flyway in each service's `src/main/resources/db/migration/`.

---

## Configuration

Runtime configuration is served by the Config Server from the `config-repo/` directory (or a Git-backed source). Each service fetches its own `.properties` file on startup.

Sensitive values (`JWT_SECRET`, database credentials, RabbitMQ credentials) are never hardcoded — they must be provided as environment variables. The application fails to start if `JWT_SECRET` is missing.

To use a Git-backed config repo instead of the local `config-repo/`:

```properties
# config-server/src/main/resources/application.properties
spring.cloud.config.server.git.uri=https://github.com/your-org/founderlink-config-repo
```

---

*Built with Java 21 · Spring Boot 3 · Spring Cloud · PostgreSQL · Redis · RabbitMQ · Docker*
