# Hardware Shop ERP

A full-stack ERP application designed to manage hardware shop operations, inventory, billing, customers, payments and business expenses.

## Technology Stack

### Backend
- Java 21
- Spring Boot 4.1.x
- Spring Data JPA
- Spring Security
- Flyway Database Migrations
- PostgreSQL 17
- Maven

### Frontend
- Next.js
- TypeScript
- Tailwind CSS

## Project Structure

- `backend/` — Spring Boot backend application
- `frontend/` — Next.js frontend application (planned)
- `docker-compose.yml` — Local PostgreSQL database configuration
- `.env.example` — Example environment configuration

## Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose

## Local Development

### 1. Configure environment variables

Copy the example configuration:

    cp .env.example .env

Update `.env` with your local database credentials.

### 2. Start PostgreSQL

    docker compose up -d

### 3. Start the backend

    cd backend
    mvn spring-boot:run

The backend runs at:

    http://localhost:8080

### 4. Verify backend health

    curl http://localhost:8080/actuator/health

## Database Migrations

Flyway manages database schema changes.

Migration files are located in:

    backend/src/main/resources/db/migration/

Migrations are applied automatically when the backend starts.

Applied migrations should never be modified. New schema changes must be introduced through new versioned migration files.

## Security

- Environment-specific credentials must remain in `.env`.
- Never commit passwords, tokens or production secrets.
- `.env` is excluded through `.gitignore`.

## Development Status

- [x] Project initialization
- [x] PostgreSQL Docker configuration
- [x] Spring Boot backend initialization
- [x] Database foundation and Flyway migrations (V1–V14)
- [x] Backend startup and database connectivity verification
- [ ] Authentication and authorization
- [ ] Backend domain entities and repositories
- [ ] Business APIs
- [ ] Next.js frontend
