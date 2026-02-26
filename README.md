<p align="center">
  Bankcards Management API for managing users, cards, and transactions in a banking system.
</p>

# Bankcards Management API

Bankcards Management API is a Java Spring Boot application that handles user management, card operations, and internal transfers. It provides role-based access for ADMIN and USER, secure authentication via JWT, and a full OpenAPI/Swagger documentation for all endpoints.

## Trust Signals

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.1-brightgreen)
![Database](https://img.shields.io/badge/Database-PostgreSQL-orange)
![License](https://img.shields.io/badge/License-MIT-green)

## Quick Start

### Prerequisites

- Java 17+
- Maven
- Docker & Docker Compose (optional, for dev environment)
- PostgreSQL (if not using Docker)

### Clone the Repository

```bash
git clone https://github.com/veqluw/card-management-system.git
cd card-management-system
```
### Set Up Environment Variables
```bash
# Database
POSTGRES_DB=service_management_db
POSTGRES_USER=username
POSTGRES_PASSWORD=password
DB_URL=jdbc:postgresql://postgres:5432/service_management_db

# Security
SECRET_ENCRYPTION_PHRASE=any_phrase_of_16_or_24_or_32_chars
JWT_SECRET=phrase_with_length_more_than_35_characters
```
### Run Locally with Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Run with Docker Compose

```bash
docker compose up --build
```
- PostgreSQL will run on port 5431  
- API will run on http://localhost:8080
- Liquibase migrations are applied automatically

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: docs/openapi.yaml

## Features
- User Management
  - Registration, login, refresh tokens, validate token, logout
  - Update profile and change password
  - Admin can view, delete, and manage all users
- Card Management
  - Create, activate, block, decline, and delete cards
  - Admin can manage all cards
  - Users can view their cards with filtering and pagination
  - Masked card numbers for security
  - Balance tracking
- Transactions
  - Transfer money between user-owned cards
  - Validation and error handling
- Security
  - JWT authentication
  - Role-based access (ADMIN, USER)
  - Password encryption and sensitive data protection
- Database
  - PostgreSQL
  - Managed with Liquibase migrations
- Deployment
  - Docker Compose for development
  - Can be run locally via Maven
  - Ready for production setup with environment variables

## Tech Stack
| Layer            | Technology             | Purpose                         |
| ---------------- |------------------------| ------------------------------- |
| Runtime          | Java 17+               | Core application                |
| Framework        | Spring Boot 4.x        | REST API & dependency injection |
| Security         | Spring Security + JWT  | Authentication & authorization  |
| Data Access      | Spring Data JPA        | ORM for PostgreSQL/MySQL        |
| DB Migrations    | Liquibase              | Schema management               |
| Containerization | Docker, Docker Compose | Dev environment & deployment    |
| API Docs         | Swagger/OpenAPI        | Endpoint documentation          |

## Project Structure

```bash
card-management-system/
├── src/main/java/test/task/bankcards/
│   ├── controller/       # REST controllers
│   ├── service/          # Business logic
│   ├── dto/              # Request and Response DTOs
│   ├── repository/       # JPA repositories
│   ├── security/         # JWT & Spring Security
│   ├── config/           # App configuration
│   └── util/             # Auxiliary classes
├── src/main/resources/
│   ├── db/changelog/     # Liquibase migrations
│   └── application.yml   # Spring Boot configuration
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── Readme_Bank_rest.md
```
## Development Workflow
### Build & Run
```bash
mvn clean install
mvn spring-boot:run
```
### Docker Dev Environment
```bash
docker-compose up --build
```
### Testing
- Unit tests for service layer
- Validation tests for controllers
- Run with Maven: mvn test

## Contributing
- Fork the repository
- Create a feature branch
- Submit pull requests with clear descriptions and test coverage
