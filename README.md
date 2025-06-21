# Martekpalce Backend

## Requirements

- **Java 17**
- **Spring Boot**
- **Maven**
- **Docker** and **Docker Compose**

## Getting Started

Follow the steps below to run the backend locally.

### 1. Install Java 17

Make sure you have Java 17 installed:

```bash
  java -version
```

### 2. Set Up Environment Variables in main module

### 3.Build and Run with Docker Compose

```bash
  docker-compose up --build
```

### 4. Access the Backend
After successful startup, the backend API will be available at: http://localhost:8080/home

---

#### Troubleshooting
Ports in use: Make sure ports 8080 (backend) and 5432 (PostgreSQL) are not already used.

Environment variables not loaded: Ensure the envs properly configured.

Docker not running: Make sure Docker Desktop or Docker Engine is running before using Docker Compose.
