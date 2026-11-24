# Room Booking API

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

Requirements: **JDK 25** and **Docker**.

```bash
./mvnw spring-boot:run
```

- The `dev` profile is active by default.
- Spring Boot's Docker Compose support starts PostgreSQL from `compose.yaml`, and Flyway creates the schema.
- In the `dev` profile, `DevUserSeeder` creates one `ADMIN` and one `USER` account for local testing. Their
  credentials are defined in that class.

The API listens on `http://localhost:8080`.

## Authentication

Exchange credentials for a token:

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email": "<email>", "password": "<password>"}'
```

```json
{ "access_token": "eyJ...", "token_type": "Bearer", "expires_in": 900 }
```

Send it on every request as `Authorization: Bearer <token>`.

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

- **The RSA key pair is generated in memory at startup.** Issued tokens stop working after a restart, and multiple
  instances can't share them. Production would load the key from a secret store or delegate to an identity provider
  (e.g. Keycloak).
- **JWTs can't be revoked before they expire.** There is no denylist and no refresh tokens. The short TTL (15 min)
  limits the impact.
- **No rate limiting on `/auth/token`.**
- **Opening hours and slot duration** (`booking.*` properties) are configured but not yet enforced on bookings.
