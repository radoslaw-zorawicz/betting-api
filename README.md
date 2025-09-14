# Betting API — Quick Run Guide

Two ways to run locally:

- **A. Containerized app + Postgres (Docker Compose)**
- **B. Native app + Dockerized Postgres**

---

## Prerequisites
- Java **24**
- Maven Wrapper (`./mvnw`)
- Docker & Docker Compose v2+

---

## Configuration
The application runs with Spring profile **`local`** and uses the configuration from:
`src/main/resources/application-local.yml`.

---

## A) Run containerized **app + db**
From the project root (where `docker-compose.yml` is):
```bash
docker compose up -d --build
docker compose logs -f betting-api   # optional: tail app logs
curl http://localhost:8080/events    # quick check
```

---

## B) Run **native app** + **dockerized db**
Start Postgres, then run the app with the `local` profile:

```bash
docker compose up -d betting-api-postgres

# (optional) run all tests first
./mvnw verify

# start the app (no JAR build needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# quick check
curl http://localhost:8080/events
```

---

## Build & Test (locally)
```bash
./mvnw clean verify
java -jar target/*.jar --spring.profiles.active=local
```

---

## Troubleshooting
- **Port 5432 in use:** stop local Postgres or change the port mapping in `docker-compose.yml`.
- **DB not ready:** after `docker compose up -d betting-api-postgres`, wait a few seconds (or use a healthcheck) before starting the app.
