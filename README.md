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

## Project decisions

## Project decisions

- **Hexagonal-inspired architecture.** We follow ports/adapters where it helps, but we don’t apply the pattern strictly—kept pragmatic to keep things simple.
- **Past events & settlement.** The task allows betting on past events. Placing bets is still possible even after an event has been settled via `POST /events/{event_id}/settlement`.
- **Multiple bets allowed.** A user can place multiple bets on the same race, including multiple bets on the same driver.
- **Identifiers.** Event IDs are **strings** (keeps the API agnostic across providers); driver IDs are **integers** (assumed equal to the driver’s race number).
- **Rate limiting.** The F1 API doesn’t publish limits, but code indicates roughly **30 requests / 10 seconds** for free users. The client has throttling to respect this.
- **Database schema.** Managed by **Liquibase**; main changelog at `src/main/resources/db/changelog/db.changelog-master.sql`.

---

## API

Base URL (local): `http://localhost:8080`
Errors follow **RFC 9457** (Problem Details for HTTP APIs).

---

### Bets

#### `GET /bets`
**Helper endpoint for testing.** Returns **all bets from the database**.

**Responses**
- `200 OK` — JSON array of bets.

**Example**
```bash
curl -s http://localhost:8080/bets
```

#### `POST /bets`
Create a new bet. **Requires user context header**.

**Headers**
- `Content-Type: application/json`
- `X-USER-ID: <1..5>` — required; DB is pre-seeded with users **1–5**.

**Request body** (snake_case; **IDs are integers**)
```json
{
  "event_id": 123,
  "driver_id": 44,
  "bet_amount": 25.0
}
```

**Responses**
- `201 Created` — bet created.
- `400 Bad Request`, `402 Payment Required`, `500 Internal Server Error` — error (RFC 9457).

**Example**
```bash
curl -s -X POST http://localhost:8080/bets \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 1" \
  -d '{"event_id":123,"driver_id":44,"bet_amount":25.0}'
```

---

### Events

#### `GET /events`
Fetch events (optional filters).

**Query params (snake_case, all optional)**
- `year` — integer
- `meeting_key` — integer
- `session_type` — string

**Responses**
- `200 OK`
- `422 Unprocessable Entity`, `429 Too Many Requests`, `500 Internal Server Error` — error (RFC 9457).

**Example**
```bash
curl -s 'http://localhost:8080/events?year=2024&meeting_key=1234&session_type=Race'
```

#### `GET /events/{session_id}/drivers_market`
Get the drivers market for a session.

**Path**
- `session_id` — string (required)

**Responses**
- `200 OK`
- `422 Unprocessable Entity`, `429 Too Many Requests`, `500 Internal Server Error` — error (RFC 9457).

**Example**
```bash
curl -s 'http://localhost:8080/events/abc123/drivers_market'
```

#### `POST /events/{event_id}/settlement`
Settle an event with the winning driver.

**Path**
- `event_id` — **integer** (required)

**Request body** (snake_case; **IDs are integers**)
```json
{
  "winning_driver_id": 44
}
```

**Responses**
- `200 OK`
- `400 Bad Request`, `409 Conflict`, `500 Internal Server Error` — error (RFC 9457).

**Example**
```bash
curl -s -X POST 'http://localhost:8080/events/123/settlement' \
  -H 'Content-Type: application/json' \
  -d '{"winning_driver_id":44}'
```

## Troubleshooting
- **Port 5432 in use:** stop local Postgres or change the port mapping in `docker-compose.yml`.
- **DB not ready:** after `docker compose up -d betting-api-postgres`, wait a few seconds (or use a healthcheck) before starting the app.
