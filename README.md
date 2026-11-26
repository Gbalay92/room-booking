# Room Booking API

[![CI](https://github.com/Gbalay92/room-booking/actions/workflows/ci.yml/badge.svg)](https://github.com/Gbalay92/room-booking/actions/workflows/ci.yml)
![Coverage](https://raw.githubusercontent.com/Gbalay92/room-booking/badges/jacoco.svg)

A REST API for booking meeting rooms. Users authenticate with a JWT, list rooms, check availability and book time
slots; admins manage rooms and see every booking. The core guarantee is that **a room can never be double-booked**,
even under concurrent requests. That guarantee is enforced in the database and covered by tests against a real
PostgreSQL.

## Tech stack

- Java 25, Spring Boot 4.1
- Spring Security 7 (OAuth2 Resource Server, RS256 JWT)
- Hibernate 7 / Spring Data JPA, PostgreSQL 17, Flyway
- JUnit 6, Mockito, Testcontainers, ArchUnit, JaCoCo

## Architecture

Hexagonal (ports and adapters):

```
infrastructure.in.web  ──►  application  ──►  domain
                             (services,         (Room, Booking, User,
                              ports)             domain exceptions)
                                ▲
infrastructure.out.persistence ─┘  implements the ports (JPA)
```

- **domain**: plain Java records with their invariants. They depend only on the JDK.
- **application**: use cases (`BookingService`, `RoomService`) and the repository ports they need.
- **infrastructure**: adapters to the outside world.
  - `in.web`: controllers, DTOs and error handling.
  - `out.persistence`: JPA entities and repositories.
  - `config` and `security`: Spring wiring.

The dependency rules are enforced by ArchUnit (`ArchitectureTests`), so a wrong import fails the build.

### Preventing double bookings

Bookings are half-open ranges `[startsAt, endsAt)`, so 10:00–11:00 and 11:00–12:00 do not overlap. Overlaps are
prevented in three layers:

1. **Service check.** `existsOverlapping` rejects the obvious case with a clear `409`.
2. **Pessimistic lock on the room.** `SELECT ... FOR UPDATE` serialises concurrent bookings for the same room.
   Transient concurrency failures are retried with backoff (`RetryingBookingService`).
3. **Database exclusion constraint.** This is the final guarantee, even if the code above is bypassed:

   ```sql
   EXCLUDE USING gist (room_id WITH =, tstzrange(starts_at, ends_at) WITH &&)
   ```

   A violation (SQLState `23P01`) is translated back into the same `409`.

Room updates use optimistic locking (`@Version`). Sending a stale `version` returns `409`.

## Getting started

### Run the full stack with Docker Compose

Requirements: **Docker** with Compose v2 and **OpenSSL**. No JDK needed.

```bash
cp .env.example .env            # then change every value in .env

mkdir -p secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt-private.pem
openssl pkey -in secrets/jwt-private.pem -pubout -out secrets/jwt-public.pem
chmod 644 secrets/*.pem         # the app runs as a non-root user inside the container

docker compose up -d --build --wait
```

The API listens on `http://localhost:8081` (`APP_PORT` in `.env`). Log in with the `ADMIN_EMAIL` /
`ADMIN_PASSWORD` from `.env` (see [Authentication](#authentication)).

What `compose.yaml` does:

- **Startup order.** The app starts only once PostgreSQL passes its `pg_isready` healthcheck. The app has its own
  healthcheck on `/actuator/health`, so `--wait` returns when the API is ready.
- **`prod` profile.** The image defaults to `SPRING_PROFILES_ACTIVE=prod`. Every sensitive value comes from the
  environment, and a missing one stops the startup instead of falling back to a default. Compose also refuses to
  start without `DB_USERNAME` / `DB_PASSWORD`.
- **Secrets.** `.env` and `secrets/` are gitignored. The RSA keys are mounted as files under `/run/secrets/`, not
  passed as environment variables, so they don't show up in `docker inspect`. Because the keys are stable, tokens
  stay valid across restarts.
- **Initial admin.** In `prod`, `InitialAdminSeeder` creates the admin from `ADMIN_EMAIL` / `ADMIN_PASSWORD` if it
  doesn't exist yet. It's safe to run on every restart.
- **Network.** The database publishes no port. Only the app can reach it, over the Compose network. Data is kept in
  the `db-data` volume (`docker compose down -v` removes it).
- **Graceful shutdown.** On `docker compose stop`, in-flight requests are allowed to finish (up to 20 s) and new ones
  are refused. `stop_grace_period` is 30 s, so Docker never kills the JVM before Spring is done.

### Run locally for development

Requirements: **JDK 25** and **Docker**.

```bash
./mvnw spring-boot:run
```

- The `dev` profile is active by default.
- Spring Boot's Docker Compose support starts PostgreSQL from `compose.dev.yaml`, and Flyway creates the schema.
- In the `dev` profile, `DevUserSeeder` creates one `ADMIN` and one `USER` account for local testing. Their
  credentials are defined in that class.
- The RSA key pair is generated in memory, so tokens stop working after a restart.

In the `dev` profile the API listens on `http://localhost:8090`.

#### Configuration

| Variable | Used by | Purpose |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `prod` | PostgreSQL connection (Compose sets `DB_URL` itself) |
| `JWT_PUBLIC_KEY`, `JWT_PRIVATE_KEY` | `prod` | PEM key locations, e.g. `file:/run/secrets/jwt-private.pem`. The private key must be PKCS#8 |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | `prod` | Initial admin account (optional) |
| `APP_PORT` | Compose | Host port for the API (default `8081`) |
| `JAVA_TOOL_OPTIONS` | image | JVM flags (default `-XX:MaxRAMPercentage=75`) |

### Docker image

```bash
docker build -t room-booking .
```

The multi-stage `Dockerfile` works in three steps:

1. **Build.** Maven builds the jar in a JDK image. Dependencies are resolved in their own layer (`dependency:go-offline`)
   before the source code is copied in.
2. **Extract.** The jar is split into Spring Boot layers (`-Djarmode=tools extract --layers`).
3. **Runtime.** Those layers are copied into a JRE-only image that runs as a non-root user.

The final image has no JDK, Maven, source code or secrets.

- **Layering.** A code change only rebuilds the `application` layer (~85 KB). The 61 MB `dependencies` layer is reused.
  This made rebuilds about 4× faster and keeps pushes and pulls small.
- **Memory.** The heap is sized from the container's memory limit with `-XX:MaxRAMPercentage=75`. A fixed `-Xmx`
  would have to be changed every time the limit changes. JVM flags can be overridden at runtime through
  `JAVA_TOOL_OPTIONS`.

#### Why a Dockerfile and not Buildpacks

`./mvnw spring-boot:build-image` (Paketo Buildpacks) was evaluated as an alternative. Both images were run with a
512 MB limit:

| | Dockerfile | Buildpacks |
|---|---|---|
| Image size | 397 MB | 382 MB |
| Startup | ~12.5 s | ~11.5 s |
| Max heap | 384 MB (75% of the limit) | 91 MB (computed `-Xmx`) |
| Shell in the image | yes (Ubuntu base) | no |
| Starts with defaults | yes | no: the memory calculator reserves 250 threads + 240 MB code cache, so it needs `BPL_JVM_THREAD_COUNT` tuned |

Size and startup time are about the same. The Dockerfile wins on control and transparency:

- every layer and JVM flag is explicit;
- the image can be debugged with a shell;
- it behaves predictably under small memory limits.

Buildpacks would be the better choice to avoid maintaining a Dockerfile across many services.

## Authentication

Exchange credentials for a token:

```bash
curl -X POST http://localhost:8081/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email": "<email>", "password": "<password>"}'
```

```json
{ "access_token": "eyJ...", "token_type": "Bearer", "expires_in": 900 }
```

Send it on every request as `Authorization: Bearer <token>`. The examples use the Compose port (`8081`). In the
`dev` profile, use `8090`.

- Tokens are signed with RS256, issued by `room-booking` and valid for 15 minutes.
- The `sub` claim is the user's email. It becomes the owner of the bookings that user creates.
- The `roles` claim (`USER` / `ADMIN`) is mapped to Spring authorities.
- The API is stateless: no sessions, no cookies. CSRF protection is therefore disabled.

## API

| Method | Path | Access |
|---|---|---|
| POST | `/auth/token` | public |
| GET | `/actuator/health` | public |
| GET | `/api/rooms`, `/api/rooms/{id}` | authenticated |
| GET | `/api/rooms/available?minCapacity=&startsAt=&endsAt=` | authenticated |
| POST | `/api/rooms` | ADMIN |
| PUT / DELETE | `/api/rooms/{id}` | ADMIN |
| GET | `/api/bookings` | USER: own bookings · ADMIN: all bookings |
| GET | `/api/bookings/{id}` | owner or ADMIN |
| POST | `/api/bookings` | authenticated |
| DELETE | `/api/bookings/{id}` | owner or ADMIN |

Timestamps are ISO-8601 instants, for example:

```
GET /api/rooms/available?minCapacity=4&startsAt=2027-03-01T10:00:00Z&endsAt=2027-03-01T11:00:00Z
```

```json
POST /api/bookings
{ "roomId": "<uuid>", "startsAt": "2027-03-01T10:00:00Z", "endsAt": "2027-03-01T11:00:00Z" }
```

`minCapacity` is optional and defaults to 1. A booking's owner is always taken from the token, never from the request
body.

## Errors

Every error is an RFC 9457 `application/problem+json` response:

| Status | When |
|---|---|
| 400 | Validation errors (with an `errors` array of `field` / `message`), inverted time range |
| 401 | Missing, invalid or expired token; wrong credentials |
| 403 | Authenticated but not allowed (e.g. a USER creating a room or cancelling someone else's booking) |
| 404 | Room or booking not found |
| 409 | Overlapping booking; room modified concurrently (stale `version`) |
| 422 | Room capacity above the configured limit |

## Testing

```bash
./mvnw verify
```

This needs a running Docker daemon, because the integration tests start PostgreSQL with Testcontainers.

| Level | What | Tools |
|---|---|---|
| Unit | Domain invariants, service logic | JUnit, Mockito |
| Web slice | Controllers, validation, security rules, error format | `@WebMvcTest`, MockMvcTester |
| Persistence slice | Queries, overlap constraint, exception translation | `@DataJpaTest` + Testcontainers |
| Integration | Login → book → conflict → forbidden cancel, over HTTP | `@SpringBootTest(RANDOM_PORT)` |
| Concurrency | Parallel bookings of the same slot → exactly one wins | `@SpringBootTest` + Testcontainers |
| Architecture | Hexagonal dependency rules | ArchUnit |

JaCoCo fails the build if line coverage of `domain` and `application` drops below 80%.

**Why Testcontainers instead of H2?** The no-overlap guarantee relies on a PostgreSQL exclusion constraint
(`btree_gist`, `tstzrange`). H2 doesn't support it, so a test against H2 would pass without testing the thing that
matters.

## Known limitations

- **The JWT signing keys are files managed by hand.** In `prod` they are loaded from PEM files, so tokens survive
  restarts and instances can share them. There is no key rotation, though. A real deployment would keep the keys in
  a secret store (e.g. AWS Secrets Manager) or delegate to an identity provider (e.g. Keycloak).
- **JWTs can't be revoked before they expire.** There is no denylist and no refresh tokens. The short TTL (15 min)
  limits the impact.
- **No rate limiting on `/auth/token`.**
- **Opening hours and slot duration** (`booking.*` properties) are configured but not yet enforced on bookings.
