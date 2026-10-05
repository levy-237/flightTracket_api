# Flight Tracker API

Track aircraft, explore recorded flight positions, and receive live flight updates over WebSocket.

> **Under construction** — this project is actively being developed. API responses and features may change. Authentication, route following, and alerts are planned and are not implemented yet.

## About

Flight Tracker API is a Spring Boot backend that periodically collects aircraft observations from an ADS-B API, stores aircraft and flight history in PostgreSQL, and broadcasts the latest flight data to connected clients.

The current tracking area is centered near Vienna Airport. Each polling cycle saves the available aircraft positions and publishes a flight update. The default delay between completed polling cycles is 31 seconds.

## Current features

- **Live updates:** STOMP over WebSocket broadcasts shared with all subscribed clients.
- **Aircraft records:** registration, aircraft type, and transponder hex identifier.
- **Flight history:** recorded flights associated with each aircraft.
- **Position history:** coordinates, altitude, speed, heading track, and timestamps associated with each flight.
- **Paginated REST API:** browse aircraft, flights, and positions with configurable page size and sorting.
- **Environment-based configuration:** database credentials, API URL, and allowed frontend origins.

## Roadmap

- [ ] **Authentication:** user registration and sign-in.
- [ ] **Follow routes:** let users follow routes they are interested in.
- [ ] **Alerts:** notify users about relevant updates for the routes they follow.
- [ ] **Immediate initial updates:** send the latest cached flight snapshot when a client subscribes.

## Built with

| Component | Technology |
| --- | --- |
| Language | Java 21 |
| Application framework | Spring Boot 4.1.1 |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Live messaging | Spring WebSocket / STOMP |
| Build | Maven Wrapper |

## Run locally

### 1. Prepare the database

Install Java 21 or newer and PostgreSQL. Create a database and a user with permission to create and update its tables. The application currently uses Hibernate's `ddl-auto=update` to manage the schema.

### 2. Configure the environment

Set the following variables in your IntelliJ run configuration or the shell that launches the application:

| Variable | Purpose | Example |
| --- | --- | --- |
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/flighttracker` |
| `DB_USERNAME` | Database username | `flighttracker_user` |
| `DB_PASSWORD` | Database password | Your local database password |
| `API_URL` | ADS-B API base URL | `https://api.adsb.lol` |
| `ALLOW_CORS` | Allowed REST frontend origins, comma-separated | `http://localhost:5173` |
| `ALLOW_SOCKET` | Allowed WebSocket origin patterns, comma-separated | `http://localhost:*` |

In IntelliJ, configure these under **Run → Edit Configurations → Environment variables** for the application. A `.env` file is not required. Keep real credentials out of committed files.

Additional settings, including the polling interval and pagination limits, live in [`application.properties`](src/main/resources/application.properties). The tracking coordinates and radius currently live in [`FlightScheduler`](src/main/java/levan/flightdetector/scheduler/FlightScheduler.java).

### 3. Start the application

Run `FlightdetectorApplication` from IntelliJ, or use the Maven Wrapper from a shell with the environment variables configured:

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

The API is available at `http://localhost:8080/api` by default. The scheduler starts collecting data automatically; it requires access to the configured ADS-B API.

## REST API

All endpoints below use `GET` and return paginated results.

| Endpoint | Description |
| --- | --- |
| `/api/aircrafts` | Browse aircraft |
| `/api/aircrafts/{aircraftId}/flights` | Browse flights belonging to an aircraft |
| `/api/flights` | Browse flights |
| `/api/flights/{flightId}/positions` | Browse positions belonging to a flight |
| `/api/flight-positions` | Browse all recorded positions |

### Pagination and sorting

```http
GET /api/aircrafts/7/flights?page=1&size=20&sort=id,desc
GET /api/flights/42/positions?page=1&size=20&sort=recordedAt,asc
```

- Default page size: **20**, with a maximum of **100**.
- Default sort: **`id,desc`** — highest IDs first.
- Request page numbers start at **1**; response `page.number` starts at **0**.
- Responses contain a `content` array and `page` metadata.

There is also an experimental, unpaginated `/api/aircraft-details` endpoint that returns nested aircraft, flights, and positions. It is intended for development and may be removed.

## Live flight updates

Connect a STOMP client to:

```text
ws://localhost:8080/ws
```

Then subscribe to:

```text
/topic/flights
```

Each broadcast contains a JSON array of flight snapshots, including aircraft identifiers, callsign, position, altitude, speed, track, vertical rate, and observation time.

Each client has its own WebSocket connection, while polling is shared within a running application instance. New subscribers currently receive the next scheduled broadcast; an immediate cached snapshot is planned.

## Tests

Run the controller pagination tests without a database:

```bash
./mvnw -Dtest=FlightsPaginationTests test
```

Run the full test suite:

```bash
./mvnw test
```

The full suite includes an application context test, which needs the required environment variables and a reachable PostgreSQL database. Starting the context also enables the flight scheduler.
