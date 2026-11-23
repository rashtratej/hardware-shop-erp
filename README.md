# Hardware Shop ERP

A backend ERP system for managing the operations of a hardware and construction materials store. The application is built using Java, Spring Boot, and PostgreSQL, with a modular architecture designed for maintainability and future expansion.

## Features

### 1. Authentication and Authorization
- User authentication using email and password.
- JWT-based authentication.
- Role-based access control.
- Supported roles: `ADMIN` and `STAFF`.
- BCrypt password hashing.
- Protected REST API endpoints.

### 2. Product Catalogue Management
- Product creation and management.
- Product categories and hierarchical categorization.
- Units of measurement (UOM).
- HSN code support.
- GST rate management.
- Product pricing.
- Reorder level configuration.
- Product activation and deactivation.
- Paginated product retrieval.

### 3. Inventory Management
- Single-location inventory management.
- Stock-in operations.
- Stock-out operations.
- Manual stock adjustments.
- Inventory balance tracking.
- Available quantity calculation.
- Low-stock detection using product reorder levels.
- Immutable-by-application stock movement history.
- Paginated stock movement history.
- Transactional inventory updates.
- Product-level pessimistic locking to prevent concurrent stock update conflicts.

**Inventory design principle:** Every stock change updates the inventory balance and creates a corresponding stock movement within the same database transaction.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.2 | Backend framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database persistence |
| Hibernate | ORM |
| PostgreSQL 17 | Relational database |
| Flyway | Database migration management |
| Maven | Build and dependency management |
| JWT | Stateless authentication |
| Docker | Database containerization |
| Postman | API testing |

## Project Structure

```text
hardware-shop-erp/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hardwarestore/erp/
│   │   │   │   ├── auth/
│   │   │   │   ├── catalogue/
│   │   │   │   ├── inventory/
│   │   │   │   ├── common/
│   │   │   │   └── BackendApplication.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       └── application.yml
│   │   │
│   │   └── test/
│   │
│   └── pom.xml
│
├── docker-compose.yml
├── .env
├── .gitignore
└── README.md
```

## Prerequisites

Install the following before running the application:

- Java Development Kit (JDK) 21
- Maven
- Docker Desktop
- Git
- Postman (for API testing)

Verify Java and Maven:

```bash
java -version
mvn -version
docker --version
```

## Environment Configuration

Create a `.env` file in the project root.

```env
DB_NAME=hardware_erp
DB_USERNAME=hardware_admin
DB_PASSWORD=your_secure_password
DB_PORT=5433

JWT_SECRET=your_secure_jwt_secret
```

**Important:**
- Replace placeholder values with your own credentials.
- Never commit `.env` to Git.
- Use a sufficiently long, cryptographically secure JWT secret.
- The Docker PostgreSQL container maps host port `5433` to container port `5432`.

## Database Setup

Start PostgreSQL using Docker Compose from the project root:

```bash
docker compose up -d postgres
```

Check the container:

```bash
docker ps
```

The PostgreSQL container should be accessible on:

```text
localhost:5433
```

Database name:

```text
hardware_erp
```

Flyway automatically manages database schema migrations when the Spring Boot application starts.

Migration files are located in:

```text
backend/src/main/resources/db/migration/
```

## Running the Backend

Navigate to the backend directory:

```bash
cd backend
```

Configure Java 21 on macOS:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

Load environment variables from the project root:

```bash
set -a
source ../.env
set +a
```

Start the Spring Boot application:

```bash
mvn spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

### Health Check

```http
GET http://localhost:8080/actuator/health
```

Expected status:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

## API Overview

Base URL:

```text
http://localhost:8080/api/v1
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Authenticate user and obtain JWT |

### Product Catalogue

The catalogue module provides APIs for managing products, categories, units of measurement, and GST rates.

### Inventory Management

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/inventory/stock-in` | ADMIN | Add stock |
| POST | `/inventory/stock-out` | ADMIN | Remove stock |
| POST | `/inventory/adjustments` | ADMIN | Adjust stock |
| GET | `/inventory/{productId}` | ADMIN, STAFF | Retrieve inventory balance |
| GET | `/inventory/{productId}/movements` | ADMIN, STAFF | Retrieve stock movement history |

Movement history supports pagination:

```text
GET /api/v1/inventory/{productId}/movements?page=0&size=20
```

### Authentication for Protected APIs

1. Log in using `/api/v1/auth/login`.
2. Obtain the JWT access token from the response.
3. Open the Authorization tab in Postman.
4. Select `Bearer Token`.
5. Paste the access token.
6. Send the protected API request.

Access tokens currently have a 15-minute validity period.

## Inventory Business Rules

- Inventory is maintained at a single location.
- Each product has one inventory balance.
- Stock cannot fall below the reserved quantity.
- Stock-in operations create positive inventory movements.
- Stock-out operations create negative inventory movements.
- Adjustments support positive and negative quantity changes.
- Every successful stock operation creates a movement record.
- Failed stock operations must not modify the inventory balance or create a movement.
- Inventory balance changes and movement records are committed transactionally.
- Stock movement records have no update or delete API.

## Development Roadmap

| Module | Status |
|---|---|
| Project Setup | Completed |
| Database Configuration | Completed |
| Authentication and Authorization | Completed |
| Product Catalogue Management | Completed |
| Inventory Management | Completed |
| Supplier Management | Planned |
| Purchase Order Management | Planned |
| Purchase Approval Workflow | Planned |
| Goods Receipt Management | Planned |
| Purchase-to-Inventory Integration | Planned |
| Sales Management | Planned |
| Customer Management | Planned |
| Invoicing and Billing | Planned |
| GST Reporting | Planned |
| Dashboard and Analytics | Planned |

## Development Guidelines

- Follow a modular package structure.
- Use DTOs for API request and response objects.
- Keep business logic inside service classes.
- Use repositories for database access.
- Manage database changes through Flyway migrations.
- Use database transactions for operations that modify multiple related records.
- Apply role-based authorization to protected endpoints.
- Validate incoming API requests.
- Avoid exposing sensitive information in API responses.
- Never commit environment secrets or credentials.

## Git Workflow

Create a feature branch for new functionality:

```bash
git checkout -b feature/module-name
```

Commit changes:

```bash
git add .
git commit -m "feat: implement module name"
```

Push changes:

```bash
git push origin feature/module-name
```

## Repository

GitHub: https://github.com/rashtratej/hardware-shop-erp

---

**Project:** Hardware Shop ERP  
**Backend:** Java 21 + Spring Boot  
**Database:** PostgreSQL  
**Architecture:** Modular REST API