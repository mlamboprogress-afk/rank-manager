# Taxi Rank Manager — Build Status

> Last updated: 2026-11-22 (added dev/local/prod profiles, docker-compose.yml)

---

## Project overview

Modular monolith using **Hexagonal Architecture (Ports and Adapters)**.
Backend: **Quarkus 3.15.1** · Database: **PostgreSQL** · Frontend: **React 18 + TypeScript + Vite**
Base package: `za.co.taxirankmanager` · Java 21 target, compiled with JDK 27

---

## Module structure

| Module | Artifact ID | Status |
|---|---|---|
| `modules/shared-kernel` | `shared-kernel` | ✅ Done |
| `modules/identity` | `module-identity` | ✅ Done |
| `modules/rank` | `module-rank` | ✅ Done |
| `modules/fleet` | `module-fleet` | ✅ Done |
| `modules/route` | `module-route` | ✅ Done |
| `modules/queue` | `module-queue` | ✅ Done |
| `modules/notification` | `module-notification` | ✅ Done |
| `adapters/inbound-rest` | `adapter-inbound-rest` | ✅ Done |
| `adapters/inbound-scheduler` | `adapter-inbound-scheduler` | ✅ Done |
| `adapters/outbound-postgres` | `adapter-outbound-postgres` | ✅ Done |
| `adapters/outbound-sms` | `adapter-outbound-sms` | ✅ Done |
| `adapters/outbound-security` | `adapter-outbound-security` | ✅ Done |
| `adapters/outbound-clock` | `adapter-outbound-clock` | ✅ Done |
| `apps/taxi-rank-manager-service` | `taxi-rank-manager-service` | ✅ Done |
| `frontend/taxi-rank-manager-web` | — | ✅ Scaffolded |

---

## What's been built

### Domain layer (pure Java — no framework dependencies)

#### Shared Kernel
- [x] `DomainException` — base for all domain exceptions
- [x] `ClockPort` — interface: `now()`, `today()`

#### Identity module
- [x] `UserId` — UUID value object
- [x] `Role` — enum: `RANK_ADMIN`, `QUEUE_MARSHAL`, `DRIVER`
- [x] `User` — aggregate with `hasRole()` helper
- [x] `CurrentUserPort` — `currentUserId()`, `currentUser()`

#### Rank module
- [x] `RankId` — UUID value object with `generate()`
- [x] `Rank` — aggregate: `create()`, `reconstitute()`, `rename()`, `deactivate()`
- [x] `RankRepository` — port (out)
- [x] `CreateRankUseCase`, `ListRanksUseCase` — ports (in)
- [x] `CreateRankCommand`
- [x] `RankService` — `@ApplicationScoped`, implements both use cases

#### Fleet module
- [x] `TaxiId`, `DriverId` — UUID value objects with `generate()`, `of()`
- [x] `VehicleRegistration` — normalises on `of()`; equality on normalised form
- [x] `SouthAfricanIdNumber` — Luhn validation; `hash()` for DB lookup; `toString()` always masked
- [x] `Taxi` — aggregate: `register()`, `reconstitute()`, `deactivate()`
- [x] `Driver` — aggregate: `register()`, `reconstitute()`, `updateContactDetails()`, `deactivate()`
- [x] `TaxiRepository`, `DriverRepository` — ports (out)
- [x] `RegisterTaxiUseCase`, `RegisterDriverUseCase`, `ListFleetUseCase` — ports (in)
- [x] `RegisterTaxiCommand`, `RegisterDriverCommand`
- [x] `FleetService` — `@ApplicationScoped`, implements all three use cases

#### Route module
- [x] `RouteId`, `RankRouteId` — UUID value objects with `generate()`, `of()`
- [x] `Route` — aggregate: `create()`, `reconstitute()`
- [x] `RankRoute` — aggregate linking `RankId` + `RouteId`: `create()`, `reconstitute()`, `deactivate()`
- [x] `RouteRepository`, `RankRouteRepository` — ports (out)
- [x] `CreateRouteUseCase`, `AssignTaxiToRankRouteUseCase`, `ListRoutesUseCase` — ports (in)
- [x] `CreateRouteCommand`, `AssignTaxiToRankRouteCommand`
- [x] `RouteService` — `@ApplicationScoped`, implements all three use cases

#### Queue module
- [x] `DailyQueueId`, `QueueEntryId`, `QueueEventId` — UUID value objects
- [x] `QueueEntryStatus` — enum: `WAITING`, `SKIPPED`, `DEPARTED` (no `CALLED`)
- [x] `QueueLifecycleStatus` — enum: `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`
- [x] `QueueEventType` — enum covering all queue events
- [x] `QueueEntry` — package-private mutation methods; `reconstitute()` public
- [x] `DailyQueue` — core aggregate with full skip invariant (skipped taxi moves to back of WAITING entries)
- [x] `QueueEvent` — immutable record with factory methods
- [x] Domain exceptions: `InvalidQueueTransitionException`, `DuplicateQueueEntryException`, `QueueEntryAlreadyDepartedException`, `QueueEntryNotFoundException`
- [x] `DailyQueueRepository`, `QueueEventRepository`, `QueueListRepository` — ports (out)
- [x] `CreateDailyQueueUseCase`, `PublishDailyQueueUseCase`, `AddEntryToQueueUseCase`, `SkipTaxiUseCase`, `DepartTaxiUseCase`, `GetQueueUseCase` — ports (in)
- [x] Commands: `CreateDailyQueueCommand`, `PublishDailyQueueCommand`, `AddEntryToQueueCommand`, `SkipTaxiCommand`, `DepartTaxiCommand`
- [x] Services: `CreateDailyQueueService`, `PublishDailyQueueService`, `AddEntryToQueueService`, `SkipTaxiService`, `DepartTaxiService`, `GetQueueService` — all `@ApplicationScoped`

#### Notification module
- [x] `NotificationId`, `NotificationEvent`, `DeliveryStatus`
- [x] `OutboxNotification` — transactional outbox aggregate: `create()`, `reconstitute()`, `markSent()`, `markFailed()`
- [x] `SmsGateway` — port (out)
- [x] `OutboxNotificationRepository` — port (out)

---

### Adapter layer

#### `adapter-outbound-clock`
- [x] `SystemClockAdapter` — `@ApplicationScoped`, `Africa/Johannesburg` timezone

#### `adapter-outbound-security`
- [x] `OidcCurrentUserAdapter` — `@RequestScoped`, maps OIDC roles to domain `Role` enum

#### `adapter-outbound-sms`
- [x] `StubSmsGateway` — `@ApplicationScoped`, logs to console; returns `"STUB-" + UUID`

#### `adapter-outbound-postgres` (JPA — no Panache)
- [x] JPA entities: `RankJpaEntity`, `RouteJpaEntity`, `RankRouteJpaEntity`, `TaxiJpaEntity`, `DriverJpaEntity`, `DailyQueueJpaEntity`, `QueueEntryJpaEntity`, `QueueEventJpaEntity`, `NotificationOutboxJpaEntity`
- [x] Repository implementations: `JpaRankRepository`, `JpaRouteRepository`, `JpaRankRouteRepository`, `JpaTaxiRepository`, `JpaDriverRepository`, `JpaDailyQueueRepository` (also implements `QueueListRepository`), `JpaQueueEventRepository`, `JpaNotificationOutboxRepository`
- [x] Flyway migration `V001__initial_schema.sql` — full schema including `notification_outbox` with PENDING partial index
- [x] Domain ID numbers stored as SHA-256 hash (lookup) + raw bytes (pending production encryption)

#### `adapter-inbound-rest`
- [x] `HealthResource` — `GET /health` → `"OK"`
- [x] `RankResource` — `GET/POST /api/ranks`, `GET /api/ranks/{id}`, `GET /api/ranks/{id}/routes`, `POST /api/ranks/rank-routes`
- [x] `RouteResource` — `GET/POST /api/routes`, `GET /api/routes/{id}`
- [x] `FleetResource` — `GET/POST /api/fleet/taxis`, `GET /api/fleet/taxis/{id}`, `GET/POST /api/fleet/drivers`, `GET /api/fleet/drivers/{id}`
- [x] `QueueResource` — `POST /api/queues`, `GET /api/queues/{id}`, `GET /api/queues?rankRouteId&date`, `GET /api/queues/by-rank-route/{id}`, `PUT /api/queues/{id}/publish`, `POST /api/queues/{id}/entries`, `PUT /api/queues/{id}/entries/{entryId}/skip`, `PUT /api/queues/{id}/entries/{entryId}/depart`
- [x] DTOs: `CreateRankRequest`, `RankResponse`, `CreateRouteRequest`, `RouteResponse`, `AssignRankRouteRequest`, `RegisterTaxiRequest`, `TaxiResponse`, `RegisterDriverRequest`, `DriverResponse`, `CreateQueueRequest`, `AddQueueEntryRequest`, `SkipTaxiRequest`, `DepartTaxiRequest`, `QueueResponse`, `QueueEntryResponse`
- [x] All mutating endpoints use `@Transactional`

#### `adapter-inbound-scheduler`
- [x] `NotificationOutboxProcessor` — `@Scheduled(every="30s")` stub (logs, no real dispatch yet)

---

### Application wiring
- [x] `apps/taxi-rank-manager-service` — depends on all 6 adapters
- [x] `application.properties` — PostgreSQL datasource, Flyway, Hibernate (no auto-DDL), HTTP 8080, OIDC disabled, OpenAPI + Swagger UI, `quarkus.index-dependency` for all 13 modules
- [x] ArchUnit tests — 6 rules enforcing hexagonal boundaries (domain ↔ Quarkus, adapters ↔ domain, etc.)
- [x] 13 domain unit tests for `DailyQueue` — all pass

---

### Frontend scaffolding
- [x] React 18 + TypeScript + Vite project at `frontend/taxi-rank-manager-web/`
- [x] Vite proxy `/api` → `localhost:8080`
- [x] Basic `App.tsx` placeholder
- [ ] No real UI components yet

---

## What's still outstanding

### Integration tests
- [ ] Testcontainers-based integration test for the PostgreSQL adapter (spin up real Postgres, run Flyway, verify repository round-trips)
- [ ] Quarkus `@QuarkusTest` for each REST resource (H2 or Testcontainers profile)
- [ ] ArchUnit test run wired into the CI pipeline

### Notification outbox processing
- [ ] Real `NotificationOutboxProcessor` implementation — load pending rows, call `SmsGateway`, mark sent/failed
- [ ] At-least-once delivery guarantee (idempotency key on the SMS gateway call)
- [ ] Real SMS gateway adapter (replace `StubSmsGateway` with an actual provider, e.g. BulkSMS, Twilio)

### Security
- [ ] Enable OIDC (`quarkus.oidc.enabled=true`) and configure the identity provider URL
- [ ] `OidcCurrentUserAdapter` — handle the case where no user is in context (unauthenticated requests to public endpoints)
- [ ] Role-based access control (`@RolesAllowed`) on REST resources
- [ ] Token validation integration test

### Business rules not yet enforced
- [ ] A taxi must be assigned to the rank-route before it can be added to that route's queue
- [ ] A driver must be linked to a taxi before the taxi can be queued
- [ ] Duplicate queue prevention per rank-route per day is in the schema (partial unique index) but not checked in the application service before persisting
- [ ] `QueueLifecycleStatus.COMPLETED` transition — no `completeQueue()` method yet on `DailyQueue`

### REST layer
- [ ] Global exception mapper — `DomainException` subclasses → 422, `IllegalArgumentException` → 400, `OptimisticLockException` → 409
- [ ] Input validation (`@NotNull`, `@NotBlank`) on request DTOs using Bean Validation
- [ ] Pagination on list endpoints (`page` + `size` query params)
- [ ] `PATCH /api/ranks/{id}` — rename a rank
- [ ] `DELETE /api/fleet/taxis/{id}` / `DELETE /api/fleet/drivers/{id}` — deactivate
- [ ] `PUT /api/queues/{id}/cancel` — cancel a queue

### Database
- [ ] `id_number_enc` currently stores raw UTF-8 bytes — needs AES encryption before production (POPIA compliance)
- [ ] `app_user` and `app_user_role` tables have no corresponding JPA entities or repository — user provisioning is implicit via OIDC but no sync job exists
- [ ] Additional Flyway migrations for schema changes (none yet — `V001` is the only migration)

### Frontend
- [ ] Queue management UI (create queue, add taxis, skip/depart view)
- [ ] Rank and route admin screens
- [ ] Fleet management screens (register taxi, register driver)
- [ ] Authentication flow (OIDC redirect)
- [ ] React Router routes wired up
- [ ] API client layer (typed fetch wrappers or a library like TanStack Query)

### DevOps / deployment
- [x] `docker-compose.yml` — PostgreSQL 16 for local development
- [ ] Docker Compose service for Keycloak (identity provider)
- [ ] Dockerfile / container image for the Quarkus service
- [ ] CI pipeline (GitHub Actions or Azure DevOps) — build, test, package
- [ ] Environment-specific application.properties profiles (`dev`, `staging`, `prod`)
- [ ] Azure Container App deployment configuration

---

## Build verification

```
mvnw.cmd -f C:\development\rank-manager\pom.xml compile   # all 15 modules compile clean
mvnw.cmd -f C:\development\rank-manager\pom.xml test -pl modules/queue -am
# Tests run: 13, Failures: 0, Errors: 0
```
