# Personal Library

![Backend Build](https://github.com/dmytmeln/personal-library/actions/workflows/spring-boot_deploy.yml/badge.svg)
![Frontend Build](https://github.com/dmytmeln/personal-library/actions/workflows/angular_deploy.yml/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_personal-library&metric=coverage)](https://sonarcloud.io/summary/overall?id=dmytmeln_personal-library)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_personal-library&metric=alert_status)](https://sonarcloud.io/summary/overall?id=dmytmeln_personal-library)

Personal library application for organizing books, authors, categories, collections, notes, quotes, reading goals, and recommendations.

## Applications

* **[Library API](library-app/README.md)**: Spring Boot backend, authentication, persistence, and recommendations.
* **[Library UI](library-ui/README.md)**: Angular web application.

## Prerequisites

* Git
* JDK 17
* Node.js and npm
* Docker

The backend includes Maven Wrapper; a system Maven installation is optional.

## Quick Start

### 1. Clone

```bash
git clone https://github.com/dmytmeln/Personal-Library.git
cd Personal-Library
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Start the Backend

Set a JWT signing secret, then run:

```bash
cd library-app
JWT_SECRET_KEY=replace-with-a-secure-secret ./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:JWT_SECRET_KEY = "replace-with-a-secure-secret"
.\mvnw.cmd spring-boot:run
```

Backend: `http://localhost:8080`

### 4. Start the Frontend

In another terminal:

```bash
cd library-ui
npm install
npm start
```

Frontend: `http://localhost:4200`

## Default Local Infrastructure

PostgreSQL configuration from `docker-compose.yml`:

* Database: `postgres`
* Username: `postgres`
* Password: `postgres`
* Port: `5432`

Component-specific configuration, build commands, and tests are documented in each application README.
