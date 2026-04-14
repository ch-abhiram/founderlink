# FounderLink

> A microservices-based platform for founders, startups, teams, and investors.

FounderLink is a backend-first platform built with Spring Boot, Spring Cloud, and Docker. It is designed to support startup creation, team collaboration, investment workflows, messaging, and notifications through a clean service-based architecture.

## Why FounderLink?

FounderLink aims to bring the startup ecosystem into one connected platform:

- founders can create and manage startup profiles
- teams can collaborate through invites and membership flows
- investors can discover startups and track investments
- users can chat, receive updates, and stay engaged in one system

## At a Glance

| Area | What it does |
| --- | --- |
| Authentication | Register, login, refresh tokens, logout, verification |
| Startup Management | Create, update, search, approve, follow startups |
| Team Collaboration | Invite members, manage status, view startup teams |
| Investments | Create investment requests and update statuses |
| Messaging | Startup conversations and inbox-style communication |
| Notifications | Event-driven notifications with read tracking |
| Platform Core | Config Server, Eureka, API Gateway, Docker setup |

## Current Architecture

FounderLink is currently focused on the backend and platform layer. The client side is planned as the next major step.

### Main Services

| Service | Responsibility | Port |
| --- | --- | --- |
| `config-server` | Centralized configuration | `8888` |
| `eureka-server` | Service discovery | `8761` |
| `api-gateway` | Request routing and JWT validation | `8083` |
| `auth-service` | Auth, refresh tokens, verification | `8089` |
| `user-service` | User profiles and roles | `9000` |
| `startup-service` | Startup lifecycle and followers | `8084` |
| `team-service` | Team invites and membership | `8085` |
| `notification-service` | Notifications and event listeners | `8086` |
| `messaging-service` | Conversations and direct messaging | `8087` |
| `investment-service` | Investment requests and tracking | `8088` |

### Supporting Stack

| Component | Usage |
| --- | --- |
| PostgreSQL | Separate database per domain |
| Redis | Fast-access token or cache support |
| RabbitMQ | Async event communication |
| Docker Compose | Full local environment orchestration |

### Request Flow

```text
Client -> API Gateway -> Domain Services
                   |-> Config Server
                   |-> Eureka Server
Domain Services -> PostgreSQL / Redis / RabbitMQ
```

## Repository Layout

```text
founderlink/
|- api-gateway/
|- auth-service/
|- config-repo/
|- config-server/
|- docker/
|- eureka-server/
|- investment-service/
|- messaging-service/
|- notification-service/
|- startup-service/
|- team-service/
|- user-service/
|- docker-compose.yml
|- .env.docker.example
```


## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Cloud
- Spring Security
- Spring Data JPA
- OpenFeign
- Resilience4j
- Flyway
- Spring AMQP
- Springdoc OpenAPI

### Infrastructure

- PostgreSQL 16
- Redis 7
- RabbitMQ 3
- Docker
- Docker Compose

## Quick Start

### Prerequisites

- Java 21
- Maven or included Maven wrappers
- Docker Desktop

### Setup

1. Copy `.env.docker.example` to `.env` if you want custom values.
2. Review:
   - `POSTGRES_USER`
   - `POSTGRES_PASSWORD`
   - `JWT_SECRET`
   - exposed host ports

### Run Everything

```bash
docker compose up --build
```

This brings up PostgreSQL, Redis, RabbitMQ, Config Server, Eureka Server, API Gateway, and all core services.

### Default Host Ports

| Component | Host Port |
| --- | --- |
| PostgreSQL | `15432` |
| Redis | `16379` |
| RabbitMQ AMQP | `15673` |
| RabbitMQ Management UI | `15672` |
| Config Server | `18888` |
| Eureka Dashboard | `18761` |
| API Gateway | `19080` |
| SonarQube | `19000` |

## Data Notes

FounderLink creates a separate PostgreSQL database for each domain:

- `auth_db`
- `user_db`
- `startup_db`
- `investment_db`
- `team_db`
- `messaging_db`
- `notification_db`

Useful notes:

- initialization scripts live in `docker/postgres/init/`
- configuration can come from local `config-repo/` or a Git-backed config source

## Testing

Each service includes unit and application-level tests.

Example:

```bash
cd auth-service
./mvnw test
```

Windows PowerShell:

```powershell
cd auth-service
.\mvnw.cmd test
```

## SonarQube

The repo includes local SonarQube support through Docker Compose plus a root [sonar-project.properties](/d:/CAPGEMINI/founderlink/sonar-project.properties) file that scans all Spring Boot services together.

### Start SonarQube

```bash
docker compose up -d postgres sonarqube
```

Open [http://localhost:19000](http://localhost:19000). On first login, SonarQube typically uses:

- username: `admin`
- password: `admin`

After logging in, create a project token in SonarQube and export it as `SONAR_TOKEN`.

### Build Before Scanning

For a fuller Java analysis, build the services first so SonarQube can use compiled bytecode and test reports. The root config is intentionally tolerant for a first scan, so you can still run SonarQube before every service has been built.

```powershell
mvn -q -s .\maven-settings.xml -f .\api-gateway\pom.xml test
mvn -q -s .\maven-settings.xml -f .\auth-service\pom.xml test
mvn -q -s .\maven-settings.xml -f .\config-server\pom.xml test
mvn -q -s .\maven-settings.xml -f .\eureka-server\pom.xml test
mvn -q -s .\maven-settings.xml -f .\investment-service\pom.xml test
mvn -q -s .\maven-settings.xml -f .\messaging-service\pom.xml test
mvn -q -s .\maven-settings.xml -f .\notification-service\pom.xml test
mvn -q -s .\maven-settings.xml -f .\startup-service\pom.xml test
mvn -q -s .\maven-settings.xml -f .\team-service\pom.xml test
mvn -q -s .\maven-settings.xml -f .\user-service\pom.xml test
```

### Run the Scan

If you have `sonar-scanner` installed locally:

```powershell
$env:SONAR_TOKEN="your-token"
sonar-scanner -Dsonar.host.url=http://localhost:19000 -Dsonar.token=$env:SONAR_TOKEN
```

If you prefer Docker instead of a local scanner:

```powershell
$env:SONAR_TOKEN="your-token"
docker run --rm `
  -e SONAR_HOST_URL="http://host.docker.internal:19000" `
  -e SONAR_TOKEN="$env:SONAR_TOKEN" `
  -v "${PWD}:/usr/src" `
  sonarsource/sonar-scanner-cli
```

This setup lets you track bugs, vulnerabilities, code smells, and duplication across the full microservices repo from one SonarQube project. Test and coverage data become richer after the individual services are built and tested.

## What Makes This Project Strong

- clear separation of business domains
- good base for scaling features independently
- event-driven support already introduced with RabbitMQ
- gateway and security structure already in place
- Docker-based local setup is ready for team development

## Future Plans

### Angular Frontend

The most important next step is a dedicated Angular frontend that turns the backend into a complete product experience.

Suggested frontend areas:

- founder dashboard
- investor dashboard
- startup discovery page
- team workspace
- notifications center
- messaging inbox
- admin review panel

Recommended Angular add-ons:

- Angular Material or PrimeNG for UI components
- Tailwind CSS for layout and design speed
- Angular Router guards and interceptors for auth
- NgRx or Signals-based state handling
- Apache ECharts or `ngx-charts` for dashboards
- form validation and reusable API service layers

### Platform Add-ons

- WebSocket or Server-Sent Events for live chat and live notifications
- Prometheus and Grafana for observability
- OpenTelemetry or Zipkin for tracing
- GitHub Actions for CI/CD
- Kubernetes or Helm for deployment
- rate limiting at the gateway
- audit logging for admin actions
- file uploads for pitch decks and startup assets
- Elasticsearch or OpenSearch for advanced search
- recommendation engine for founder-investor matching

### Product Roadmap

- startup ranking and discovery feed
- watchlists and saved startups
- richer investor-founder matching
- onboarding wizard for different user roles
- startup verification workflow
- activity timeline and digest emails
- collaboration comments on startup updates

## Suggested Next Milestones

1. Add `frontend/` with an Angular application.
2. Expose cleaner, centralized API documentation.
3. Add integration tests for core user journeys.
4. Set up CI pipelines for build, test, and image publishing.
5. Add monitoring, tracing, and real-time support.

## Contribution Direction

If this project keeps growing, a strong direction would be:

- keep every service independently buildable
- avoid premature shared modules unless the contracts are stable
- document service-level environment variables clearly
- keep the gateway as the main client entry point
- add architecture decisions as the platform evolves
