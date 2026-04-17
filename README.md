# SampleLogin API

A production-ready JWT authentication REST API built with **Spring Boot 4**, **PostgreSQL**, and **Docker**. Implements short-lived access tokens, rotating refresh tokens, account lockout, audit logging, user management, notifications, system settings, and user preferences.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Project Structure](#project-structure)
- [API Reference](#api-reference)
- [Authentication Flow](#authentication-flow)
- [Seed Accounts](#seed-accounts)
- [Database](#database)
- [CORS Configuration](#cors-configuration)
- [Running Tests](#running-tests)
- [Docker Reference](#docker-reference)
- [Troubleshooting](#troubleshooting)

---

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Framework   | Spring Boot 4.0.5                   |
| Language    | Java 25                             |
| Security    | Spring Security 6 + JJWT 0.12      |
| Database    | PostgreSQL 17                       |
| ORM         | Spring Data JPA / Hibernate 6       |
| Validation  | Jakarta Validation                  |
| Build       | Maven 3.9                           |
| Container   | Docker + Docker Compose             |
| Utilities   | Lombok                              |

---

## Prerequisites

| Tool           | Version  | Download                    |
|----------------|----------|-----------------------------|
| Docker Desktop | 4.x+     | docker.com/get-started      |
| JDK 25         | 25 LTS   | java.com/download           |
| Maven          | 3.9+     | maven.apache.org            |
| IntelliJ IDEA  | 2024.1+  | jetbrains.com/idea          |
| Git            | 2.x      | git-scm.com                 |
| DBeaver        | Latest   | dbeaver.io *(optional)*     |
| Postman        | Latest   | postman.com *(optional)*    |

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/adgtomaquin/samplelogin.git
cd samplelogin
```

### 2. Build and start the full stack

```bash
docker compose up --build
```

This starts two services:
- **`authdb`** — PostgreSQL 17 on port `5432`
- **`sampleapi`** — Spring Boot app on port `8080`

The app waits for PostgreSQL to pass its health check before starting. On first run, `schema.sql` and `data.sql` are executed automatically to create tables and insert seed data.

### 3. Verify the app is running

```
GET http://localhost:8080/actuator/health
```

Expected response:
```json
{ "status": "UP" }
```

---

## Environment Variables

All variables have defaults suitable for local development. Override them in `docker-compose.yml` or via a `.env` file for production.

| Variable               | Default                                              | Description                        |
|------------------------|------------------------------------------------------|------------------------------------|
| `DB_HOST`              | `localhost`                                          | PostgreSQL host                    |
| `DB_PORT`              | `5432`                                               | PostgreSQL port                    |
| `DB_NAME`              | `authdb`                                             | Database name                      |
| `DB_USER`              | `authuser`                                           | Database username                  |
| `DB_PASS`              | `authpass`                                           | Database password                  |
| `JWT_SECRET`           | *(hex string)*                                       | HMAC-SHA256 signing key            |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173`         | Comma-separated allowed origins    |

> **Production:** Always override `JWT_SECRET` with a cryptographically random value of at least 32 bytes. Never use the default.

Copy `.env.example` to `.env` and fill in production values:

```bash
cp .env.example .env
```

---

## Project Structure

```
src/main/java/com/example/auth/
├── AuthApplication.java
│
├── config/
│   ├── JwtAuthFilter.java          # Extracts + validates JWT on every request
│   └── SecurityConfig.java         # Spring Security filter chain + CORS config
│
├── controller/
│   ├── AdminController.java        # GET  /admin/stats
│   ├── AuditLogController.java     # GET  /audit-logs, /audit-logs/stats
│   ├── AuthController.java         # POST /auth/login, /refresh, /logout; GET /auth/me
│   ├── NotificationController.java # GET/PATCH/DELETE /notifications
│   ├── PreferencesController.java  # GET/PATCH /preferences
│   ├── SettingsController.java     # GET/PATCH /settings/system
│   └── UserController.java         # CRUD /users
│
├── dto/                            # Request and response objects
├── entity/                         # JPA entities
├── exception/
│   ├── AuthException.java          # Domain exception with HTTP status
│   └── GlobalExceptionHandler.java # @RestControllerAdvice error handler
├── repository/                     # Spring Data JPA repositories
└── service/
    ├── AuditLogService.java
    ├── AuthService.java            # Login, refresh, logout logic
    ├── JwtService.java             # Token generation + parsing (JJWT 0.12)
    ├── NotificationService.java
    ├── PreferencesService.java
    ├── SettingsService.java
    └── UserService.java

src/main/resources/
├── application.properties          # App configuration
├── schema.sql                      # DDL — runs on every startup (IF NOT EXISTS)
└── data.sql                        # Seed data — runs on every startup (ON CONFLICT DO NOTHING)

src/test/resources/
├── application-test.properties     # H2 in-memory config for tests
├── schema.sql                      # H2-compatible DDL
└── data.sql                        # Test seed data
```

---

## API Reference

Base URL: `http://localhost:8080`

Protected endpoints require:
```
Authorization: Bearer <access_token>
```

### Authentication

| Method | Endpoint        | Auth | Description                                      |
|--------|-----------------|------|--------------------------------------------------|
| POST   | `/auth/login`   | No   | Login with email + password → returns token pair |
| POST   | `/auth/refresh` | No   | Exchange refresh token → new token pair          |
| POST   | `/auth/logout`  | Yes  | Revoke refresh token / all sessions              |
| GET    | `/auth/me`      | Yes  | Get current user profile                         |

### Users *(ADMIN only)*

| Method | Endpoint      | Auth  | Description                  |
|--------|---------------|-------|------------------------------|
| GET    | `/users`      | ADMIN | List users (filter + search) |
| POST   | `/users`      | ADMIN | Invite a new user            |
| GET    | `/users/{id}` | ADMIN | Get user by ID               |
| PATCH  | `/users/{id}` | ADMIN | Update user                  |
| DELETE | `/users/{id}` | ADMIN | Delete user                  |

### Notifications

| Method | Endpoint                   | Auth | Description                    |
|--------|----------------------------|------|--------------------------------|
| GET    | `/notifications`           | Yes  | List notifications for user    |
| POST   | `/notifications/read-all`  | Yes  | Mark all notifications as read |
| PATCH  | `/notifications/{id}`      | Yes  | Mark single notification read  |
| DELETE | `/notifications/{id}`      | Yes  | Delete a notification          |

### Audit Logs *(ADMIN only)*

| Method | Endpoint            | Auth  | Description                          |
|--------|---------------------|-------|--------------------------------------|
| GET    | `/audit-logs`       | ADMIN | List audit logs (filter + paginate)  |
| GET    | `/audit-logs/stats` | ADMIN | Severity counts (info/warning/critical) |

### System Settings *(ADMIN only)*

| Method | Endpoint           | Auth  | Description                    |
|--------|--------------------|-------|--------------------------------|
| GET    | `/settings/system` | ADMIN | Get current system settings    |
| PATCH  | `/settings/system` | ADMIN | Update system settings         |

### User Preferences

| Method | Endpoint       | Auth | Description                       |
|--------|----------------|------|-----------------------------------|
| GET    | `/preferences` | Yes  | Get current user's preferences    |
| PATCH  | `/preferences` | Yes  | Update current user's preferences |

### Admin Dashboard

| Method | Endpoint       | Auth  | Description                |
|--------|----------------|-------|----------------------------|
| GET    | `/admin/stats` | ADMIN | Total users, active today, new this week, system load |

### Example: Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "admin@example.com", "password": "Admin@1234", "remember_me": false }'
```

Response:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "550e8400-e29b-41d4-a716-446655440000",
  "token_type": "Bearer",
  "expires_in": 900,
  "refresh_expires_in": 604800,
  "user": {
    "id": "a0000000-0000-0000-0000-000000000001",
    "email": "admin@example.com",
    "name": "Alex Rivera",
    "avatar": "AR",
    "roles": ["ADMIN", "USER"],
    "department": "Engineering"
  }
}
```

---

## Authentication Flow

```
Client                          Server
  │                               │
  │  POST /auth/login             │
  │ ─────────────────────────────>│  Validate credentials
  │                               │  Check account lockout
  │  { access_token, refresh_token } │
  │ <─────────────────────────────│  Issue JWT (15 min) + refresh token (7 days)
  │                               │
  │  GET /auth/me                 │
  │  Authorization: Bearer <jwt>  │
  │ ─────────────────────────────>│  Validate JWT signature + expiry
  │  { user profile }             │
  │ <─────────────────────────────│
  │                               │
  │  POST /auth/refresh           │
  │  { refresh_token }            │
  │ ─────────────────────────────>│  Verify token is not revoked/expired
  │  { new access_token, new refresh_token } │  Revoke old token (rotation)
  │ <─────────────────────────────│  Issue new pair
  │                               │
  │  POST /auth/logout            │
  │  Authorization: Bearer <jwt>  │
  │  { refresh_token }            │
  │ ─────────────────────────────>│  Revoke refresh token
  │  204 No Content               │
  │ <─────────────────────────────│
```

**Security features:**
- Access tokens expire in **15 minutes** (configurable)
- Refresh tokens expire in **7 days** (30 days with `remember_me: true`)
- Refresh token **rotation** — every refresh issues a new token and revokes the old one
- **Replay detection** — submitting a previously used refresh token immediately revokes all sessions for that user
- **Account lockout** — 5 consecutive failed logins locks the account for 15 minutes (configurable via `/settings/system`)
- Passwords hashed with **BCrypt** (strength 10)

---

## Seed Accounts

Created automatically on first startup via `data.sql`.

| Email                 | Password     | Roles        |
|-----------------------|--------------|--------------|
| `admin@example.com`   | `Admin@1234` | ADMIN, USER  |
| `user@example.com`    | `User@1234`  | USER         |

---

## Database

### Schema

Tables created automatically by `schema.sql` on startup (uses `CREATE TABLE IF NOT EXISTS`):

| Table                | Description                                      |
|----------------------|--------------------------------------------------|
| `users`              | User accounts with status, lockout, login stats  |
| `user_roles`         | Many-to-one role assignments per user            |
| `refresh_tokens`     | Server-side refresh token store with revocation  |
| `auth_audit_log`     | Immutable log of all auth and admin events       |
| `notifications`      | Per-user and broadcast notifications             |
| `user_preferences`   | Display, timezone, and notification preferences  |
| `system_settings`    | Singleton row of platform-wide configuration     |
| `rate_limit_buckets` | Token bucket state for rate limiting             |

### Connecting with DBeaver

| Field    | Value       |
|----------|-------------|
| Host     | `localhost` |
| Port     | `5432`      |
| Database | `authdb`    |
| Username | `authuser`  |
| Password | `authpass`  |

> Docker must be running before DBeaver can connect.

---

## CORS Configuration

CORS is configured in `SecurityConfig.java` via a `CorsConfigurationSource` bean wired directly into the Spring Security filter chain. The allowed origins are driven by the `CORS_ALLOWED_ORIGINS` environment variable.

```properties
# application.properties
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
```

**Important:** `WebMvcConfigurer.addCorsMappings()` does **not** work with Spring Security — Spring Security's filter chain runs before Spring MVC and will block preflight `OPTIONS` requests before they reach MVC. Always configure CORS through `SecurityConfig`.

For production, set the env var to your actual frontend domain:

```yaml
# docker-compose.yml (production)
CORS_ALLOWED_ORIGINS: https://yourapp.com
```

---

## Running Tests

Tests use an H2 in-memory database with the `test` profile — no Docker required.

```bash
# Run all tests
mvn test

# Run with IntelliJ
# Right-click src/test → Run All Tests
```

The `application-test.properties` file in `src/test/resources` configures H2 with `MODE=PostgreSQL` for maximum compatibility.

---

## Docker Reference

### Common commands

```bash
# Start everything (builds image on first run)
docker compose up --build

# Start in background
docker compose up --build -d

# Stop containers (preserves database volume)
docker compose down

# Stop and delete database volume (fresh start)
docker compose down -v

# Force full rebuild — use after Java/config changes
docker compose build --no-cache
docker compose up

# View live app logs
docker compose logs -f app

# Check container status
docker compose ps
```

### Services

| Service    | Container  | Port | Description            |
|------------|------------|------|------------------------|
| `postgres` | `authdb`   | 5432 | PostgreSQL 17 Alpine   |
| `app`      | `sampleapi`| 8080 | Spring Boot API        |

### Volumes

| Volume          | Description                      |
|-----------------|----------------------------------|
| `postgres_data` | Persists PostgreSQL data between restarts |

---

## Troubleshooting

### `column "name" of relation "users" does not exist`

The PostgreSQL volume contains an old schema. Wipe it and restart:

```bash
docker compose down -v
docker compose up --build
```

If the volume persists:

```bash
docker volume ls
docker volume rm samplelogin-sb4_postgres_data -f
docker compose up --build
```

---

### Old code still running after code changes

Docker cached the old build layers. Force a clean rebuild:

```bash
docker compose down -v
docker compose build --no-cache
docker compose up
```

Also clear all dangling build cache if needed:

```bash
docker builder prune -f
```

---

### `Access-Control-Allow-Origin` header missing

The image was not rebuilt after CORS changes were made. Verify the header is present:

```powershell
# PowerShell
curl.exe -v -X OPTIONS http://localhost:8080/auth/login `
  -H "Origin: http://localhost:3000" `
  -H "Access-Control-Request-Method: POST" `
  -H "Access-Control-Request-Headers: content-type"
```

Expected: `Access-Control-Allow-Origin: http://localhost:3000` in the response.

If missing, force a full rebuild:

```bash
docker compose down -v
docker compose build --no-cache
docker compose up
```

---

### App container exits immediately

Check the logs:

```bash
docker compose logs app --tail=100
```

Common causes:
- `DB_HOST` is wrong — in Docker Compose it must be `postgres` (the service name), not `localhost`
- Missing `JWT_SECRET` environment variable
- PostgreSQL not yet healthy when app started (the health check should prevent this)

---

### Port 8080 or 5432 already in use

```powershell
# Find what is using the port (PowerShell)
netstat -ano | findstr :8080

# Kill by PID
taskkill /PID <PID> /F
```

Or change the host port in `docker-compose.yml`:

```yaml
ports:
  - "8081:8080"   # host:container
```

---

### Cannot connect to Docker daemon

Docker Desktop is not running. Start it from the system tray or Start Menu and wait for the green status indicator before retrying.

---

### `||` is not a valid statement separator (PowerShell)

PowerShell does not support `||`. Use separate commands or `-f` flag instead:

```powershell
# Instead of:  docker rmi my-image 2>/dev/null || true
# Use:
docker rmi my-image -f
```

---

### `grep` is not recognized (PowerShell)

Use `Select-String` instead:

```powershell
docker compose exec app jar tf /app/app.jar | Select-String "SecurityConfig"
```
