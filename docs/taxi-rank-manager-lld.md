# Taxi Rank Manager – Low-Level Design (LLD)

## Design Overview

| | |
|---|---|
| **Application Name** | Taxi Rank Manager |
| **Backend Service** | `taxi-rank-manager-service` |
| **Domain / Bounded Context** | Taxi Rank Operations |
| **Java Base Package** | `za.co.taxirankmanager` (proposed; confirm naming convention) |
| **Description** | A South African taxi-rank queue management platform that replaces paper-based daily taxi queues and verbal announcements. Rank administrators and queue marshals manage ranks, routes, taxis, drivers and published daily queues through a responsive web application. Drivers can access their queue information through the web application and receive SMS notifications without installing a mobile application or having mobile data. |
| **Phase** | Phase 1 – Responsive Web Application + SMS |
| **Author** | Product / Engineering – to be confirmed |
| **Status** | DRAFT – for architecture and product review |

### Stakeholders

| Stakeholder | Responsibility |
|---|---|
| Product owner / business sponsor | Confirms operating procedures, notification policy and acceptance criteria |
| Rank administrators | Manage assigned ranks, routes, taxis, drivers and marshals |
| Queue marshals | Prepare, publish and operate daily queues for assigned routes |
| Taxi drivers | View queues associated with their taxis and receive SMS notifications |
| Engineering / architecture | Owns technical design, implementation and operation |

---

## Table of Contents

- [Scope](#scope)
- [Out of Scope](#out-of-scope)
- [Assumptions and Constraints](#assumptions-and-constraints)
- [Decisions and Open Decisions](#decisions-and-open-decisions)
- [Architecture](#architecture)
- [APIs](#apis)
- [Service Logic](#service-logic)
- [Class Definitions (Implementation Contract)](#class-definitions-implementation-contract)
- [Data Model](#data-model)
- [Integrations](#integrations)
- [Non-Functional Requirements](#non-functional-requirements)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Deployment and Operations](#deployment-and-operations)
- [Appendices](#appendices)

---

## Scope

### In Scope – Phase 1

1. Authentication and authorisation for exactly three application roles: `RANK_ADMIN`, `QUEUE_MARSHAL` and `DRIVER`.
2. Rank administration, including administrators who are authorised to manage multiple ranks and can switch between those ranks.
3. Route management within a rank. A route represents a service operating from a particular rank, for example Witbank → Pretoria.
4. Driver and taxi registration and their association.
5. South African identity-number validation and storage controls for driver identity information.
6. South African vehicle registration-number capture, normalisation and uniqueness.
7. Taxi registration at multiple ranks, with exactly one active route assignment for a taxi at each individual rank.
8. Allocation of queue marshals to one or more rank-route assignments.
9. Daily queue preparation, ordering, publication, progression and history.
10. Queue entry statuses: `WAITING`, `DEPARTED` and `SKIPPED` only.
11. Queue audit history and revision history.
12. SMS notifications to drivers through a provider-independent integration.
13. A responsive web experience for rank administrators, queue marshals and drivers.
14. Driver access to queues for all taxis associated with the authenticated driver profile.
15. Reporting and historical views sufficient to inspect daily queue activity.
16. Operational controls for SMS attempts, failures, retries and delivery receipts where the selected provider supports them.

### Out of Scope – Phase 1

1. Native Android or iOS driver applications.
2. Push notifications / Firebase Cloud Messaging.
3. Mandatory smartphone ownership or application installation for drivers.
4. Microservices, Kafka or a distributed event-streaming platform.
5. Offline-first queue operation at a rank. The effect of a rank losing internet connectivity is an open operational decision.
6. Payments, fare collection, passenger booking or passenger-facing functionality.
7. GPS tracking, vehicle tracking or automatic geofencing.
8. Automated identity verification against Home Affairs or other identity providers.
9. Automated verification of vehicle ownership or registration against eNaTIS.
10. Association-level or provincial-level management unless added through an approved requirement.
11. A separate system-administrator application role. The initial application role model contains exactly the three roles listed above.
12. Guaranteed SMS delivery or proof that a driver has read an SMS.

---

## Assumptions and Constraints

1. **Phase 1 is a web application.** Rank administrators and marshals use the responsive web interface. Drivers may use the web interface, but SMS is the primary notification channel and does not require an app.
2. **SMS does not require mobile data.** It does require the driver's phone to have cellular service, a valid number and the ability to receive SMS. A driver in an area without cellular coverage may receive the message later or not at all.
3. **SMS is an external dependency.** The application records submission and provider delivery status where available. Provider acceptance is not proof of delivery or reading.
4. **Exactly three application roles exist:** `RANK_ADMIN`, `QUEUE_MARSHAL` and `DRIVER`. Rank and route scope is assigned separately from the role.
5. **Administrators may manage multiple ranks.** Their access is represented by explicit rank assignments. The active rank in the user interface is a selection, not an authorisation boundary by itself.
6. **A queue marshal is scoped to assigned rank-route combinations.** A marshal cannot manage queues for an unassigned route.
7. **A taxi may be registered at multiple ranks.** At any one rank, the taxi may have only one active route assignment. The same taxi may therefore operate different route directions from different ranks.
8. **Route direction is rank-relative.** Witbank → Pretoria at Witbank and Pretoria → Witbank at Pretoria are separate rank-route services.
9. **Queue instances are independent by rank, route and service date.** A taxi's position or status in one rank's queue does not affect its queue entry at another rank.
10. **The same taxi may occur at most once in a given daily queue.**
11. **The queue statuses are exactly `WAITING`, `DEPARTED` and `SKIPPED`.** `CALLED` is not a queue status. `TAXI_CALLED_TO_QUEUE` is a notification/event.
12. **Skipped taxi behaviour:** a skipped taxi is moved to the back of the waiting order. When it is eligible for another turn, its current queue-entry status is `WAITING`; the audit history retains the skip event.
13. **Departed is terminal for that queue entry.** A departed taxi is not reinserted into the same queue entry.
14. **A driver may be associated with one or more taxis.** The exact driver-to-taxi cardinality, co-driver handling and ownership/authorisation process require product confirmation. The data model must not assume a driver can see unrelated taxis.
15. **A taxi may have one or more drivers over time.** Active and historical driver-taxi relationships should be represented, but the number of simultaneously active drivers requires confirmation.
16. **The application validates South African ID number structure and checksum.** This is validation of format and mathematical consistency, not proof of identity.
17. **Vehicle registration validation must accommodate South African provincial, legacy and personalised formats.** A single hard-coded regular expression is not an adequate national validation strategy. Validation rules should be configurable and reviewed against current registration conventions.
18. **Registration numbers are unique system-wide after normalisation.** Display formatting is retained separately from the normalised comparison value.
19. **No SMS provider has been selected.** The integration must be provider-independent. Provider, sender identity, throughput, delivery receipts, pricing and regulatory requirements are procurement/open decisions.
20. **No authentication / identity provider has been selected.** The LLD defines the application authorisation model but leaves the external identity-provider contract open.
21. **The backend is a Quarkus modular monolith using Java 21.** The domain and application layers remain framework-independent.
22. **The initial database is PostgreSQL.** Database migrations are managed by Flyway.
23. **The first release is online-first for rank operations.** If the rank's internet connection fails, the backend cannot process queue actions or dispatch SMS until connectivity is restored. A local/offline queue mode is not assumed.
24. **Time and date rules:** queue service dates and notification schedules use the South African business timezone, `Africa/Johannesburg`, unless the business later requires multiple timezones.
25. **The system must retain an audit trail** for queue changes, publication, reordering, skip/depart actions, assignment changes and notification delivery attempts.

---

## Decisions and Open Decisions

### Confirmed Design Decisions

| # | Area | Decision | Rationale |
|---|---|---|---|
| D01 | Architecture | Hexagonal Architecture (Ports and Adapters) | Keeps business rules independent of frameworks and external systems |
| D02 | Deployment shape | Modular monolith | Phase 1 does not justify distributed services and their operational cost |
| D03 | Backend | Quarkus + Java 21 | Modern Java runtime with suitable REST, persistence, security and testing ecosystem |
| D04 | Frontend | Responsive React + TypeScript web application | One web client supports desktop, tablet and mobile browser use |
| D05 | Database | PostgreSQL + Flyway | Relational integrity, transactions, constraints and migration control |
| D06 | Notifications | SMS in Phase 1 | Drivers do not need mobile data or an installed app to receive notifications |
| D07 | Notification integration | Provider-independent outbound port | SMS provider can be changed without modifying queue/domain logic |
| D08 | User roles | Exactly `RANK_ADMIN`, `QUEUE_MARSHAL`, `DRIVER` | Confirmed initial role model |
| D09 | Taxi-route assignment | One active route per taxi per rank; multiple ranks allowed | Reflects real operating arrangements and prevents conflicting same-rank assignments |
| D10 | Queue statuses | `WAITING`, `DEPARTED`, `SKIPPED` only | Confirmed operational model |
| D11 | Skip behaviour | Move to back of waiting order; retain skip event in audit history | Preserves queue fairness and historical traceability |
| D12 | Queue isolation | Queue state is scoped to rank-route-service date | Operations at one rank must not affect another rank |
| D13 | Queue notification | Notification events are separate from queue statuses | A taxi being called is an event, not a fourth status |
| D14 | Notification reliability | Transactional outbox and persisted delivery attempts | Queue commits must not lose the notification work |
| D15 | Persistence boundary | Domain models are not ORM entities | Prevents database/framework concerns leaking into the domain |
| D16 | Testing | Domain tests run without Quarkus or PostgreSQL | Enforces domain independence and fast feedback |

### Open Decisions – Must Be Resolved Before Implementation

| # | Decision required | Options / impact | Current status |
|---|---|---|---|
| O01 | Queue publication behaviour | Does publication mean every included driver must immediately join the physical queue, or does it only establish order and drivers are called as their turn approaches? | Open |
| O02 | SMS recipient policy | SMS to all drivers; SMS only to drivers without app (not applicable in Phase 1); or configurable driver preference. Phase 1 has no app, so define whether all relevant SMS events are sent by default. | Open |
| O03 | SMS event thresholds | Confirm whether two-ahead, one-ahead and called-to-queue messages are all required, and whether any should be suppressed to manage cost. | Open |
| O04 | Driver onboarding | Admin-created driver accounts, self-registration, or both. | Open |
| O05 | Driver-to-taxi relationship | Can multiple drivers be actively linked to one taxi, and can a driver be actively linked to multiple taxis? | Open |
| O06 | Phone number changes | Who can change a driver's number and what verification is required? | Open |
| O07 | Queue revision after publication | Whether a published queue can be republished, who can do so, and whether a reason is mandatory. | Open |
| O08 | Daily queue creation | Whether a queue is unique per rank-route-service-date, and whether multiple sessions/shifts per route per day are needed. | Proposed unique daily queue; confirm sessions |
| O09 | Taxi unavailability | Whether marshals can exclude an otherwise eligible taxi during queue preparation and what reason is required. | Open |
| O10 | SMS provider | Select provider, sender ID, delivery receipt support, throughput, cost and South African coverage. | Open |
| O11 | Authentication provider | Select identity provider, login method, password reset and account recovery. | Open |
| O12 | Rank connectivity failure | Manual paper fallback only, or future offline capture/synchronisation. | Open |
| O13 | Queue ordering authority | Confirm who can reorder after publication and whether all changes require a reason. | Open |
| O14 | Data retention | Retention period for ID numbers, driver records, queue history, audit logs and SMS content. | Open |
| O15 | SMS language | English only initially or configurable preferred language per driver. | Open |
| O16 | SMS charging and limits | Who pays, budget thresholds, per-rank usage limits and alerting. | Open |

> ⚠️ No implementation should silently choose an answer for an open decision that changes the business process.

---

## Architecture

### High-Level Architecture

The application is a modular monolith with strict hexagonal boundaries. The deployable backend is one Quarkus application. React is a separate web client.

```
┌─────────────────────────────────────────────────────────────────────┐
│                 Taxi Rank Manager – Quarkus Application             │
│                                                                     │
│  ┌────────────────┐    ┌──────────────────────────────────────────┐ │
│  │ Authentication │    │         Inbound REST Adapters            │ │
│  │ / Authorisation│───▶│                                          │ │
│  │    Adapter     │    └──────────────────┬───────────────────────┘ │
│  └────────────────┘                       │                         │
│                                           ▼                         │
│                              ┌────────────────────────┐            │
│                              │   Application Use Cases │            │
│                              └───────────┬────────────┘            │
│                                          │                          │
│                              ┌───────────▼────────────┐            │
│                              │      Domain Model       │            │
│                              │  Rank · Taxi · Driver   │            │
│                              │  Route · DailyQueue     │            │
│                              └───────────┬────────────┘            │
│                                          │                          │
│                              ┌───────────▼────────────┐            │
│                              │    Outbound Ports       │            │
│                              └────┬──────────────┬────┘            │
│                                   │              │                  │
│               ┌───────────────────▼──┐    ┌──────▼───────────────┐ │
│               │  PostgreSQL Adapter  │    │  SMS Provider Adapter │ │
│               └──────────┬───────────┘    └──────────┬────────────┘ │
│                          │                            │              │
│          ┌───────────────▼────────┐    ┌─────────────▼──────────┐  │
│          │  Outbox / Retry        │    │                        │  │
│          │  Scheduler             │    │                        │  │
│          └────────────────────────┘    │                        │  │
└─────────────────────────────────────────────────────────────────────┘
            │                                           │
            ▼                                           ▼
      ┌──────────┐                              ┌──────────────┐
      │PostgreSQL│                              │  SMS Gateway │
      └──────────┘                              └──────┬───────┘
                                                       │ SMS
                                                       ▼
  ┌──────────────────────┐                    ┌──────────────────┐
  │ Responsive Web App   │◀── HTTPS/JSON ────▶│  Driver mobile   │
  │ React + TypeScript   │                    │  phone (SMS)     │
  └──────────────────────┘                    └──────────────────┘
```

### Hexagonal Dependency Rules

Dependencies point **inward only**:

- Inbound adapters depend on application input ports.
- Application services depend on domain types and application-owned output ports.
- Domain code depends on no framework, database, transport or provider code.
- Outbound adapters implement ports declared by the application layer.
- The composition root wires implementations through Quarkus CDI.
- The domain must not import Quarkus, Jakarta, Hibernate, Panache, Jackson, RESTEasy or SMS SDK types.
- REST request/response DTOs, persistence entities and domain objects are separate models.
- No REST resource may contain business rules or directly call a repository adapter.
- No domain aggregate may call a repository, SMS gateway or other infrastructure service.

### Proposed Backend Modules

The following is the logical module structure. Whether each module is a separate Maven module or a package-enforced module is a build decision; the dependency boundaries are mandatory either way.

```
taxi-rank-manager/
├── apps/
│   └── taxi-rank-manager-service/        # Quarkus composition root
│
├── modules/
│   ├── shared-kernel/
│   │
│   ├── identity/
│   │   ├── domain/
│   │   └── application/
│   │
│   ├── rank/
│   │   ├── domain/
│   │   └── application/
│   │
│   ├── fleet/
│   │   ├── domain/
│   │   └── application/
│   │
│   ├── route/
│   │   ├── domain/
│   │   └── application/
│   │
│   ├── queue/
│   │   ├── domain/
│   │   └── application/
│   │
│   └── notification/
│       ├── domain/
│       └── application/
│
├── adapters/
│   ├── inbound/
│   │   ├── rest/
│   │   └── scheduler/
│   │
│   └── outbound/
│       ├── postgres/
│       ├── sms/
│       ├── security/
│       └── clock/
│
└── frontend/
    └── taxi-rank-manager-web/
```

### Technology Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Backend framework | Quarkus |
| Architecture | Hexagonal Architecture, modular monolith |
| REST | Quarkus REST + Jackson |
| Validation | Jakarta Bean Validation at adapter boundary |
| Persistence | Hibernate ORM / JPA in PostgreSQL adapter only |
| Database | PostgreSQL |
| Migrations | Flyway |
| Security | Quarkus Security; OIDC/JWT integration subject to provider selection |
| API contract | OpenAPI / SmallRye OpenAPI |
| Scheduling | Quarkus Scheduler for outbox processing and retries |
| Frontend | React + TypeScript |
| Testing | JUnit 5, Mockito, REST Assured, Testcontainers |
| Architecture tests | ArchUnit or equivalent dependency checks |
| Build | Maven |
| Packaging | Docker |
| Hosting | Azure-compatible container hosting; final Azure service to be confirmed |
| Notifications | SMS provider behind an outbound port |

### Transaction and Consistency Strategy

1. Each business use case defines its transaction boundary.
2. A queue state change and its audit event are committed atomically.
3. A notification outbox record is committed in the same transaction as the business change that caused it.
4. SMS is sent asynchronously after the database transaction commits.
5. The provider call is never made while holding a database transaction open.
6. The notification processor claims pending outbox records safely so multiple application instances do not send the same event concurrently.
7. Delivery attempts are persisted separately from the business event.
8. Queue mutations use optimistic versioning and/or row-level locking to prevent two marshals from silently overwriting each other's ordering changes.
9. Database unique constraints are the final guard against duplicate active taxi assignments and duplicate queue entries.
10. External SMS delivery is at-least-once from the application's perspective unless the provider offers a reliable idempotency facility. Duplicate prevention is implemented using application notification keys and delivery state.

---

## APIs

### API Conventions

- Base path: `/api/v1`.
- Transport: HTTPS.
- Payload format: JSON, except where a future file-upload feature is explicitly approved.
- Authentication: Bearer token from the selected identity provider; exact issuer, audience and claims remain open.
- The authenticated identity is mapped to an internal user and application role.
- Rank and route scope is checked by the application use case, not trusted from a client-supplied rank identifier alone.
- IDs are opaque UUIDs in public API contracts.
- Timestamps use ISO 8601 with timezone information.
- Service dates use `YYYY-MM-DD` and are interpreted in `Africa/Johannesburg`.
- List endpoints support pagination.
- Mutating requests should support idempotency where a client may retry after a timeout.
- The API contract should be authored contract-first in OpenAPI before implementation.

### Proposed API Endpoints

| Use Case | Method | Endpoint |
|---|---|---|
| Current user profile and permissions | `GET` | `/api/v1/me` |
| List ranks available to current user | `GET` | `/api/v1/me/ranks` |
| Create rank | `POST` | `/api/v1/ranks` |
| Get rank | `GET` | `/api/v1/ranks/{rankId}` |
| Update rank | `PATCH` | `/api/v1/ranks/{rankId}` |
| Add rank administrator | `POST` | `/api/v1/ranks/{rankId}/administrators` |
| Create route at rank | `POST` | `/api/v1/ranks/{rankId}/routes` |
| List rank routes | `GET` | `/api/v1/ranks/{rankId}/routes` |
| Assign marshal to rank-route | `PUT` | `/api/v1/rank-routes/{rankRouteId}/marshals/{userId}` |
| Register driver | `POST` | `/api/v1/drivers` |
| Get driver | `GET` | `/api/v1/drivers/{driverId}` |
| Register taxi | `POST` | `/api/v1/taxis` |
| Get taxi | `GET` | `/api/v1/taxis/{taxiId}` |
| Assign taxi to rank-route | `PUT` | `/api/v1/rank-routes/{rankRouteId}/taxis/{taxiId}` |
| End taxi route assignment | `DELETE` | `/api/v1/rank-routes/{rankRouteId}/taxis/{taxiId}` |
| Search eligible taxis | `GET` | `/api/v1/rank-routes/{rankRouteId}/taxis/search` |
| Link driver to taxi | `PUT` | `/api/v1/taxis/{taxiId}/drivers/{driverId}` |
| Create daily queue | `POST` | `/api/v1/rank-routes/{rankRouteId}/queues` |
| Get daily queue | `GET` | `/api/v1/queues/{queueId}` |
| Reorder draft queue | `PUT` | `/api/v1/queues/{queueId}/order` |
| Publish queue | `POST` | `/api/v1/queues/{queueId}/publish` |
| Mark taxi departed | `POST` | `/api/v1/queues/{queueId}/entries/{entryId}/depart` |
| Skip taxi | `POST` | `/api/v1/queues/{queueId}/entries/{entryId}/skip` |
| Get queue history | `GET` | `/api/v1/rank-routes/{rankRouteId}/queues` |
| Driver's queues | `GET` | `/api/v1/drivers/me/queues` |
| Driver's notification history | `GET` | `/api/v1/drivers/me/notifications` |
| Get notification delivery status (authorised users) | `GET` | `/api/v1/notifications/{notificationId}` |

### API Contract Notes

1. `rankId`, `rankRouteId`, `taxiId`, `driverId`, `queueId` and `entryId` must be validated against the authenticated user's permissions.
2. A marshal cannot assign taxis to routes or manage users unless separately granted the rank-admin role.
3. A rank administrator may administer only ranks to which that administrator is assigned.
4. A driver can only retrieve queues for taxis currently or historically associated with that driver according to the approved visibility policy.
5. The skip endpoint changes the entry status to `SKIPPED`, records the event, and moves the taxi behind the other waiting entries. The exact reactivation moment and status transition need to be implemented consistently with the confirmed operating procedure.
6. The depart endpoint changes the entry to `DEPARTED`; a departed entry cannot be skipped or departed again.
7. Publishing is idempotent for the same queue revision. Repeated requests must not generate duplicate publication events or duplicate SMS notifications.
8. Queue reordering and skip operations must include a version or equivalent concurrency control token. A stale mutation returns `409 Conflict`.
9. The API must not expose full South African ID numbers in ordinary responses.
10. The API must not return SMS provider credentials or internal provider error details.

### Validation

| Input | Validation |
|---|---|
| Driver South African ID | 13 digits; validate encoded date, permitted structural values and checksum. Does not establish identity. |
| Taxi registration | Trim and normalise for comparison; apply configurable South African registration rules; preserve display value |
| Driver mobile number | Validate and normalise to E.164; verify possession through an approved onboarding process |
| Rank name | Required; length and uniqueness scope to be confirmed |
| Route origin/destination | Required; route must belong to the selected rank |
| Queue service date | Valid date; use South African business timezone |
| Queue entry | Taxi must have an active assignment to that rank-route and must not already occur in the queue |
| Queue order | Each included entry appears exactly once; positions are valid and deterministic |
| Queue status | Only `WAITING`, `DEPARTED`, `SKIPPED` |
| Role and scope | Authenticated user must have the required role and rank/route assignment |

---

## Service Logic

### 1. Register Taxi and Assign It to a Rank Route

**Trigger:** REST API – `POST /api/v1/taxis`, followed by assignment to a rank-route.

**Process Flow**

1. Rank administrator submits taxi registration details.
2. REST adapter validates request shape and required fields.
3. Adapter maps the request to `RegisterTaxiCommand`.
4. `RegisterTaxiUseCase` checks the caller's rank-administration authority for the requested operation.
5. Application service normalises the registration number using the registration normaliser port or domain value object.
6. `Taxi` validates its domain invariants.
7. `TaxiRepository` checks and persists the unique normalised registration number; the database unique constraint is authoritative under concurrency.
8. Driver associations are recorded according to the approved onboarding flow.
9. A separate `AssignTaxiToRankRouteUseCase` assigns the taxi to a route at a rank.
10. The application verifies that the route belongs to the rank and that the caller administers that rank.
11. The repository and database enforce one active assignment per `(taxi_id, rank_id)`.
12. The assignment is audited.

> **Important rule:** assigning the same taxi to a second rank is allowed. Assigning it to a second active route at the same rank is rejected.

### 2. Create and Publish Daily Queue

**Trigger:** REST API – `POST /api/v1/rank-routes/{rankRouteId}/queues`, followed by `POST /api/v1/queues/{queueId}/publish`.

**Process Flow**

1. Marshal selects an assigned rank-route and service date.
2. The application validates the marshal's assignment to that rank-route.
3. The application loads all active taxi assignments for the rank-route.
4. Eligible taxis are made available to the marshal. The current proposal is to include all eligible taxis by default; the ability to exclude unavailable taxis and the required reason remain an open decision (O09).
5. Marshal may search eligible taxis by registration number or driver details.
6. Marshal confirms the taxis and their order.
7. The application validates that each selected taxi is assigned to this rank-route and appears only once.
8. A draft daily queue and ordered queue entries are persisted.
9. On publication, the application revalidates queue eligibility and ordering.
10. `DailyQueue.publish()` applies domain rules and changes the queue to its published state. Queue lifecycle state is distinct from queue-entry status.
11. The queue, publication audit event and notification outbox records are committed in one transaction.
12. An asynchronous processor sends the relevant SMS messages.
13. The marshal sees the published queue and any notification processing failures visible to authorised users.

> **Queue identity:** the proposed identity is `(rank_route_id, service_date)` for one daily queue. If a route can operate multiple independent sessions or shifts on the same day, the key must be extended before implementation (O08).

### 3. Mark Taxi Departed

**Trigger:** REST API – `POST /api/v1/queues/{queueId}/entries/{entryId}/depart`.

**Process Flow**

1. Marshal requests departure for a queue entry.
2. Application verifies the marshal is assigned to the queue's rank-route.
3. Queue is loaded with a concurrency-control mechanism.
4. Domain verifies that the entry belongs to the queue and is not already departed.
5. Entry status changes to `DEPARTED`.
6. A `TAXI_DEPARTED` audit event is recorded. This is an audit event, not an additional queue status.
7. Queue progression is recalculated.
8. Any newly eligible notification events are added to the outbox.
9. Queue changes, audit event and outbox records commit atomically.
10. API returns the updated queue view.

### 4. Skip Taxi

**Trigger:** REST API – `POST /api/v1/queues/{queueId}/entries/{entryId}/skip`.

**Process Flow**

1. Marshal requests that a taxi be skipped.
2. Application verifies route assignment and queue ownership.
3. Domain rejects the operation if the entry is already `DEPARTED`.
4. Domain records a skip event with actor, time and optional reason.
5. The entry is moved to the back of the waiting order.
6. The current status is `SKIPPED` for the skip operation; when the taxi becomes eligible for another chance, its status returns to `WAITING`. The historical skip event remains immutable.
7. Other waiting taxis retain their relative order.
8. The operation and audit history are committed atomically.
9. A `TAXI_SKIPPED` notification event is created if the approved notification policy requires it.
10. The updated queue is returned.

> ⚠️ The exact point at which a skipped entry changes back to `WAITING` must be explicit in the queue domain rules and acceptance tests. It must not be implemented as an arbitrary UI-side change.

### 5. Notification Dispatch

**Trigger:** Pending outbox event.

**Process Flow**

1. Queue use case commits a business event and outbox record in the same database transaction.
2. Notification processor claims pending records using a concurrency-safe claim mechanism.
3. Processor resolves the event's intended driver recipients using the taxi-driver relationship and the approved recipient policy.
4. Processor creates a notification record and one or more delivery attempts.
5. SMS adapter maps the application notification to provider-specific request format.
6. Provider response is mapped to a provider-independent delivery result.
7. Delivery attempt is updated with provider message reference and status.
8. Retryable failures are scheduled with bounded backoff; permanent failures are marked and surfaced for operational review.
9. Provider delivery receipts, when supported, update delivery state through an authenticated inbound callback adapter.
10. Duplicate event processing is prevented using a deterministic notification key.

### Queue and Notification Events

The following events are proposed. The final notification policy must be confirmed (O02, O03).

| Event | Meaning | Queue status change? |
|---|---|---|
| `DAILY_QUEUE_PUBLISHED` | A daily queue containing the taxi has been published | No |
| `TAXI_TWO_AHEAD` | Two waiting taxis are ahead of this taxi | No |
| `TAXI_ONE_AHEAD` | One waiting taxi is ahead of this taxi | No |
| `TAXI_CALLED_TO_QUEUE` | Driver should join the physical loading queue now | No |
| `TAXI_SKIPPED` | Taxi was skipped and moved to the back | Entry becomes `SKIPPED` |
| `QUEUE_CANCELLED` | Published queue was cancelled | Queue lifecycle event |
| `QUEUE_REVISED` | Published queue was changed and republished | Queue lifecycle event |
| `TAXI_DEPARTED` | Taxi departed; retained for audit and progression | Entry becomes `DEPARTED` |

> `TAXI_CALLED_TO_QUEUE` is an event, not a queue-entry status. No `CALLED` status may be introduced.

---

## Class Definitions (Implementation Contract)

> The names below are proposed contracts. They are not a mandate to create one class per noun; keep the model cohesive and avoid an anemic domain model.

### Package Structure

```
za.co.taxirankmanager/
├── domain/
│   ├── identity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── UserId.java
│   ├── rank/
│   │   ├── Rank.java
│   │   ├── RankId.java
│   │   └── RankAdministratorAssignment.java
│   ├── fleet/
│   │   ├── Taxi.java
│   │   ├── TaxiId.java
│   │   ├── VehicleRegistration.java
│   │   ├── Driver.java
│   │   ├── SouthAfricanIdNumber.java
│   │   └── DriverTaxiAssignment.java
│   ├── route/
│   │   ├── Route.java
│   │   ├── RankRoute.java
│   │   ├── RankRouteId.java
│   │   ├── TaxiRouteAssignment.java
│   │   └── MarshalRouteAssignment.java
│   ├── queue/
│   │   ├── DailyQueue.java
│   │   ├── QueueEntry.java
│   │   ├── QueueEntryStatus.java
│   │   ├── QueueLifecycleStatus.java
│   │   ├── QueueEvent.java
│   │   └── QueuePosition.java
│   └── notification/
│       ├── Notification.java
│       ├── NotificationEvent.java
│       └── DeliveryStatus.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── RegisterTaxiUseCase.java
│   │   │   ├── AssignTaxiToRankRouteUseCase.java
│   │   │   ├── CreateDailyQueueUseCase.java
│   │   │   ├── PublishDailyQueueUseCase.java
│   │   │   ├── ReorderQueueUseCase.java
│   │   │   ├── DepartTaxiUseCase.java
│   │   │   ├── SkipTaxiUseCase.java
│   │   │   ├── GetDriverQueuesUseCase.java
│   │   │   └── ProcessPendingNotificationsUseCase.java
│   │   └── out/
│   │       ├── TaxiRepository.java
│   │       ├── DriverRepository.java
│   │       ├── RankRepository.java
│   │       ├── RankRouteRepository.java
│   │       ├── DailyQueueRepository.java
│   │       ├── QueueEventRepository.java
│   │       ├── NotificationOutboxRepository.java
│   │       ├── SmsGateway.java
│   │       ├── CurrentUserPort.java
│   │       ├── ClockPort.java
│   │       └── MobileNumberVerificationPort.java
│   ├── command/
│   ├── query/
│   └── service/
│
├── adapters/
│   ├── in/
│   │   ├── rest/
│   │   │   ├── RankResource.java
│   │   │   ├── TaxiResource.java
│   │   │   ├── DriverResource.java
│   │   │   ├── QueueResource.java
│   │   │   ├── DriverPortalResource.java
│   │   │   ├── dto/
│   │   │   ├── mapper/
│   │   │   └── exception/
│   │   └── scheduler/
│   │       └── NotificationOutboxScheduler.java
│   └── out/
│       ├── postgres/
│       │   ├── entity/
│       │   ├── repository/
│       │   └── mapper/
│       ├── sms/
│       │   ├── SmsProviderClient.java
│       │   └── SmsGatewayAdapter.java
│       ├── security/
│       │   └── CurrentUserAdapter.java
│       └── clock/
│           └── SystemClockAdapter.java
│
└── bootstrap/
    ├── configuration/
    └── TaxiRankManagerApplication.java
```

### Domain Aggregates and Responsibilities

| Aggregate / Model | Responsibility and invariants |
|---|---|
| `Rank` | Rank identity and core rank details |
| `RankAdministratorAssignment` | Which users can administer which ranks |
| `Taxi` | Taxi identity and normalised registration number |
| `Driver` | Driver profile and protected identity/contact data |
| `DriverTaxiAssignment` | Time-bounded association between driver and taxi |
| `Route` | Route definition, such as origin and destination |
| `RankRoute` | A route service operating from a specific rank |
| `TaxiRouteAssignment` | Active taxi assignment to a rank-route; one active assignment per taxi per rank |
| `MarshalRouteAssignment` | Marshal's authority to operate a rank-route |
| `DailyQueue` | Queue aggregate root for one rank-route and service date/session |
| `QueueEntry` | Taxi's ordered participation and current queue status |
| `QueueEvent` | Immutable history of queue actions |
| `Notification` | Driver-facing notification and its delivery lifecycle |

### Input Ports (Use Cases)

```java
public interface PublishDailyQueueUseCase {
    PublishQueueResult publish(PublishDailyQueueCommand command);
}

public interface SkipTaxiUseCase {
    QueueSnapshot skip(SkipTaxiCommand command);
}

public interface DepartTaxiUseCase {
    QueueSnapshot depart(DepartTaxiCommand command);
}

public interface GetDriverQueuesUseCase {
    DriverQueuesView getQueues(DriverId driverId);
}
```

### Output Ports

```java
public interface DailyQueueRepository {
    Optional<DailyQueue> findById(DailyQueueId id);
    Optional<DailyQueue> findForUpdate(DailyQueueId id);
    void save(DailyQueue queue);
}

public interface SmsGateway {
    SmsSubmissionResult send(SmsMessage message);
}

public interface NotificationOutboxRepository {
    List<PendingNotification> claimPending(int batchSize);
    void markSubmitted(NotificationId id, ProviderMessageReference reference);
    void markFailed(NotificationId id, DeliveryFailure failure);
}
```

> These are illustrative contracts. The final signatures must reflect transaction handling, concurrency control and error semantics. Repository ports return domain models or application read models, not JPA entities.

### Domain Invariants

**Taxi and rank-route**

- A taxi's normalised registration is globally unique.
- A taxi may have assignments at multiple ranks.
- A taxi may have no more than one active route assignment at a given rank.
- A route assignment must reference a route service belonging to the same rank.
- Ending an assignment preserves its historical record.

**Daily queue**

- A queue belongs to exactly one rank-route and one service date/session.
- A taxi may appear only once in a queue.
- A taxi must be actively assigned to the queue's rank-route when added, subject to the approved historical queue policy.
- Queue order is deterministic and contains no duplicate positions.
- `DEPARTED` is terminal.
- `SKIPPED` causes the taxi to move behind the other waiting taxis.
- Other waiting taxis retain relative order after a skip.
- A later turn for a skipped taxi restores its current status to `WAITING`, while the skip event remains in history.
- A queue entry cannot be changed by a marshal who is not assigned to its rank-route.
- A published queue mutation must be audited and follow the approved revision policy.

**Authorisation**

- Role alone is insufficient for rank-scoped actions.
- `RANK_ADMIN` actions require rank-administrator assignment for the target rank.
- `QUEUE_MARSHAL` actions require marshal assignment for the target rank-route.
- `DRIVER` reads are limited to the driver's associated taxis and permitted queue history.
- Client-supplied user IDs, roles or rank scopes are never trusted as proof of authority.

---

## Data Model

### Design Principles

1. Relational data is stored in PostgreSQL.
2. Domain aggregates and persistence entities are separate.
3. Foreign keys and unique constraints enforce critical integrity rules in addition to domain validation.
4. All mutable business records have audit metadata where appropriate.
5. Historical relationships are retained using effective dates or active-state history, not destructive overwrites.
6. Queue events are append-only.
7. Queue entry current state is stored for efficient reads; queue events provide the historical record.
8. South African ID numbers and mobile numbers are personal information and require restricted access and protection.
9. The full ID number must not be exposed in ordinary API responses or application logs.
10. Store a normalised vehicle registration value for uniqueness and search, while retaining a display value.
11. The exact encryption/key-management design must be confirmed against the hosting environment and POPIA assessment.
12. The schema below is logical and requires review before Flyway migrations are authored.

### Proposed Tables

#### `app_user`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Internal user ID |
| `identity_subject` | `VARCHAR(255)` | NOT NULL, UNIQUE | Subject from selected identity provider |
| `email` | `VARCHAR(255)` | NULL | Login/contact email if available |
| `mobile_number` | `VARCHAR(32)` | NULL | Normalised contact number where applicable |
| `role` | `VARCHAR(32)` | NOT NULL | `RANK_ADMIN`, `QUEUE_MARSHAL`, `DRIVER` |
| `active` | `BOOLEAN` | NOT NULL | Account enabled flag |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | Last update |

> If a person can hold more than one role, replace the single `role` column with a user-role association table. This is an open implementation decision; the model must not accidentally prevent an approved multi-role user.

#### `driver`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Driver ID |
| `user_id` | `UUID` | FK, UNIQUE | Associated login account |
| `full_name` | `VARCHAR(255)` | NOT NULL | Driver's name |
| `id_number_ciphertext` | `BYTEA / TEXT` | NOT NULL | Encrypted South African ID number |
| `id_number_lookup_hash` | `BYTEA` | UNIQUE | Keyed hash for duplicate detection, if required |
| `mobile_number` | `VARCHAR(32)` | NOT NULL | Normalised phone number |
| `mobile_verified_at` | `TIMESTAMPTZ` | NULL | Verification timestamp |
| `active` | `BOOLEAN` | NOT NULL | Driver active state |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | Last update |

> The keyed lookup hash must use a separately managed secret/key and must not be a plain unsalted hash of the ID number. Whether the ID number is mandatory at initial onboarding and the lawful basis/retention period require business and privacy review.

#### `taxi`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Taxi ID |
| `registration_display` | `VARCHAR(32)` | NOT NULL | Human-readable plate |
| `registration_normalized` | `VARCHAR(32)` | NOT NULL, UNIQUE | Normalised comparison value |
| `make` | `VARCHAR(100)` | NULL | Optional vehicle make |
| `model` | `VARCHAR(100)` | NULL | Optional vehicle model |
| `active` | `BOOLEAN` | NOT NULL | Taxi active state |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | Last update |

#### `driver_taxi_assignment`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Relationship ID |
| `driver_id` | `UUID` | FK | Driver |
| `taxi_id` | `UUID` | FK | Taxi |
| `valid_from` | `TIMESTAMPTZ` | NOT NULL | Start of association |
| `valid_to` | `TIMESTAMPTZ` | NULL | End of association |
| `created_by` | `UUID` | FK | Actor |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Audit timestamp |

> A partial unique constraint may be needed to enforce the approved active relationship cardinality. Do not add a one-driver-per-taxi constraint until the business confirms the rule (O05).

#### `rank`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Rank ID |
| `name` | `VARCHAR(255)` | NOT NULL | Rank name |
| `address` | `TEXT` | NULL | Physical address / location |
| `active` | `BOOLEAN` | NOT NULL | Active state |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | Update time |

#### `rank_administrator`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `rank_id` | `UUID` | PK, FK | Rank |
| `user_id` | `UUID` | PK, FK | Rank administrator |
| `assigned_at` | `TIMESTAMPTZ` | NOT NULL | Assignment time |
| `assigned_by` | `UUID` | FK | Assigning actor |

#### `route`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Route identity |
| `origin` | `VARCHAR(255)` | NOT NULL | Origin |
| `destination` | `VARCHAR(255)` | NOT NULL | Destination |
| `description` | `TEXT` | NULL | Optional route detail |
| `active` | `BOOLEAN` | NOT NULL | Active state |

#### `rank_route`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Rank-route identity |
| `rank_id` | `UUID` | FK | Operating rank |
| `route_id` | `UUID` | FK | Route definition |
| `display_name` | `VARCHAR(255)` | NULL | Optional rank-specific label |
| `active` | `BOOLEAN` | NOT NULL | Active state |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |

> The same route definition may be associated with different ranks. Directional routes are distinct route definitions or distinct rank-route services as appropriate.

#### `taxi_route_assignment`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Assignment ID |
| `taxi_id` | `UUID` | FK | Taxi |
| `rank_id` | `UUID` | FK | Rank, stored to support the critical uniqueness rule |
| `rank_route_id` | `UUID` | FK | Rank-route |
| `valid_from` | `TIMESTAMPTZ` | NOT NULL | Assignment start |
| `valid_to` | `TIMESTAMPTZ` | NULL | Assignment end; null means active |
| `assigned_by` | `UUID` | FK | Actor |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |

> **Critical constraint:** at most one active assignment for `(taxi_id, rank_id)`. In PostgreSQL this can be enforced with a partial unique index on `(taxi_id, rank_id) WHERE valid_to IS NULL`. A composite foreign key or equivalent constraint should ensure `rank_id` matches the rank of `rank_route_id`.

#### `marshal_rank_route`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Assignment ID |
| `user_id` | `UUID` | FK | Marshal user |
| `rank_route_id` | `UUID` | FK | Assigned rank-route |
| `valid_from` | `TIMESTAMPTZ` | NOT NULL | Assignment start |
| `valid_to` | `TIMESTAMPTZ` | NULL | Assignment end |
| `assigned_by` | `UUID` | FK | Rank administrator |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |

#### `daily_queue`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Queue ID |
| `rank_route_id` | `UUID` | FK | Rank-route |
| `service_date` | `DATE` | NOT NULL | Local service date |
| `session_key` | `VARCHAR(64)` | NULL | Reserved for multiple daily sessions if approved |
| `lifecycle_status` | `VARCHAR(32)` | NOT NULL | Draft, published, cancelled or completed lifecycle; final enum to be confirmed |
| `revision` | `INTEGER` | NOT NULL | Published revision number |
| `version` | `BIGINT` | NOT NULL | Optimistic concurrency version |
| `published_at` | `TIMESTAMPTZ` | NULL | Publication time |
| `published_by` | `UUID` | FK, NULL | Publishing actor |
| `created_by` | `UUID` | FK | Creator |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | Update time |

> **Proposed uniqueness:** `(rank_route_id, service_date, session_key)` with null handling defined explicitly. If only one queue per route/day is confirmed, use a unique constraint on `(rank_route_id, service_date)`.

#### `queue_entry`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Entry ID |
| `daily_queue_id` | `UUID` | FK | Parent queue |
| `taxi_id` | `UUID` | FK | Taxi |
| `position` | `INTEGER` | NOT NULL | Current order |
| `status` | `VARCHAR(16)` | NOT NULL | `WAITING`, `DEPARTED`, `SKIPPED` |
| `version` | `BIGINT` | NOT NULL | Entry version |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Added time |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | Update time |

**Constraints:**
- Unique `(daily_queue_id, taxi_id)`.
- Unique `(daily_queue_id, position)` or an equivalent safe ordering strategy.
- `CHECK status IN ('WAITING','DEPARTED','SKIPPED')`.
- Position changes must be performed transactionally. If a unique position constraint is used, use a collision-safe reorder strategy (for example, temporary positions or deferred constraints).

#### `queue_event`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Event ID |
| `daily_queue_id` | `UUID` | FK | Queue |
| `queue_entry_id` | `UUID` | FK, NULL | Related entry |
| `event_type` | `VARCHAR(64)` | NOT NULL | Queue event |
| `previous_status` | `VARCHAR(16)` | NULL | Prior status |
| `new_status` | `VARCHAR(16)` | NULL | New status |
| `previous_position` | `INTEGER` | NULL | Prior position |
| `new_position` | `INTEGER` | NULL | New position |
| `reason` | `TEXT` | NULL | Reason where required |
| `actor_user_id` | `UUID` | FK | User or system actor |
| `occurred_at` | `TIMESTAMPTZ` | NOT NULL | Event time |
| `metadata` | `JSONB` | NULL | Non-sensitive event context |

> Queue events are append-only. Corrections are represented by subsequent events, not by editing history.

#### `notification_outbox`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Outbox event ID |
| `event_key` | `VARCHAR(255)` | NOT NULL, UNIQUE | Deterministic deduplication key |
| `event_type` | `VARCHAR(64)` | NOT NULL | Notification event type |
| `aggregate_id` | `UUID` | NOT NULL | Queue or queue-entry ID |
| `payload` | `JSONB` | NOT NULL | Minimal event payload; avoid unnecessary personal data |
| `status` | `VARCHAR(24)` | NOT NULL | `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED` |
| `attempt_count` | `INTEGER` | NOT NULL DEFAULT 0 | Processing attempts |
| `next_attempt_at` | `TIMESTAMPTZ` | NULL | Retry schedule |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Creation time |
| `processed_at` | `TIMESTAMPTZ` | NULL | Completion time |

#### `notification`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Notification ID |
| `driver_id` | `UUID` | FK | Intended driver |
| `queue_id` | `UUID` | FK, NULL | Related queue |
| `queue_entry_id` | `UUID` | FK, NULL | Related taxi entry |
| `event_type` | `VARCHAR(64)` | NOT NULL | Notification event |
| `channel` | `VARCHAR(16)` | NOT NULL | SMS in Phase 1 |
| `deduplication_key` | `VARCHAR(255)` | NOT NULL, UNIQUE | Prevent duplicate logical notification |
| `message_template_key` | `VARCHAR(100)` | NOT NULL | Template identifier |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | Created time |

#### `notification_delivery_attempt`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Attempt ID |
| `notification_id` | `UUID` | FK | Notification |
| `provider` | `VARCHAR(64)` | NOT NULL | Provider name |
| `provider_message_id` | `VARCHAR(255)` | NULL | Provider reference |
| `status` | `VARCHAR(24)` | NOT NULL | `SUBMITTED`, `DELIVERED`, `FAILED`, `UNKNOWN` |
| `failure_code` | `VARCHAR(100)` | NULL | Normalised failure code |
| `failure_detail` | `TEXT` | NULL | Sanitised provider detail |
| `attempted_at` | `TIMESTAMPTZ` | NOT NULL | Attempt time |
| `delivered_at` | `TIMESTAMPTZ` | NULL | Delivery receipt time |

#### `audit_log`

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | PK | Audit record |
| `actor_user_id` | `UUID` | NULL for system | Actor |
| `action` | `VARCHAR(100)` | NOT NULL | Business action |
| `aggregate_type` | `VARCHAR(100)` | NOT NULL | Aggregate type |
| `aggregate_id` | `UUID` | NOT NULL | Aggregate ID |
| `occurred_at` | `TIMESTAMPTZ` | NOT NULL | Action time |
| `correlation_id` | `VARCHAR(100)` | NULL | Request correlation |
| `details` | `JSONB` | NULL | Minimal before/after context |

> Do not put full ID numbers, passwords, authentication tokens or SMS secrets into audit details.

### Entity Relationship Diagram

```
APP_USER ||--o| DRIVER : "may have profile"
APP_USER ||--o{ RANK_ADMINISTRATOR : administers
RANK ||--o{ RANK_ADMINISTRATOR : has
APP_USER ||--o{ MARSHAL_RANK_ROUTE : assigned
RANK_ROUTE ||--o{ MARSHAL_RANK_ROUTE : has
RANK ||--o{ RANK_ROUTE : operates
ROUTE ||--o{ RANK_ROUTE : configured
TAXI ||--o{ DRIVER_TAXI_ASSIGNMENT : associated
DRIVER ||--o{ DRIVER_TAXI_ASSIGNMENT : associated
TAXI ||--o{ TAXI_ROUTE_ASSIGNMENT : assigned
RANK ||--o{ TAXI_ROUTE_ASSIGNMENT : constrains
RANK_ROUTE ||--o{ TAXI_ROUTE_ASSIGNMENT : serves
RANK_ROUTE ||--o{ DAILY_QUEUE : has
DAILY_QUEUE ||--o{ QUEUE_ENTRY : contains
TAXI ||--o{ QUEUE_ENTRY : appears
DAILY_QUEUE ||--o{ QUEUE_EVENT : records
QUEUE_ENTRY ||--o{ QUEUE_EVENT : records
DAILY_QUEUE ||--o{ NOTIFICATION_OUTBOX : emits
DRIVER ||--o{ NOTIFICATION : receives
NOTIFICATION ||--o{ NOTIFICATION_DELIVERY_ATTEMPT : attempts
```

### Queue Ordering and Concurrency

- Every queue mutation is performed by an application use case inside a database transaction.
- The queue aggregate is loaded with a version or row lock before ordering changes.
- Concurrent mutation attempts must not result in duplicate positions, lost updates or duplicate departures.
- A stale client version returns `409 Conflict` with the latest queue version available to the caller.
- A skip operation preserves the relative order of all other waiting entries and places the skipped taxi after the current waiting tail.
- `DEPARTED` entries are excluded from active waiting-position calculations.
- The business definition of position display (absolute list position versus position among waiting taxis) must be confirmed and then used consistently in UI, API and SMS templates.

---

## Integrations

### SMS Gateway

**Purpose:** Deliver queue notifications to drivers in Phase 1.

**Boundary:** `SmsGateway` is an outbound application port. The provider SDK, HTTP client, credentials, callback format and provider-specific response codes are confined to the SMS adapter.

**Required capabilities:**

1. Submit an SMS to a normalised mobile number.
2. Return a provider-independent submission result.
3. Expose provider message reference where available.
4. Support delivery receipt callbacks where the provider offers them.
5. Map provider errors into retryable and permanent categories.
6. Apply bounded retries and rate limits.
7. Record provider, submission time, response and delivery state.
8. Avoid logging message content or personal data unnecessarily.
9. Support provider configuration through secrets/configuration management.
10. Allow a future provider replacement without changes to domain or queue use cases.

> **Provider selection:** Clickatell, BulkSMS and Africa's Talking were identified as candidates for evaluation. No provider or price is selected by this LLD. Obtain current South African pricing, sender-ID requirements, throughput limits, delivery receipt support, support arrangements and data-processing terms before procurement (O10).

### SMS Templates

Templates should be version-controlled and configurable without embedding provider-specific message formatting in queue logic.

| Key | Purpose |
|---|---|
| `DAILY_QUEUE_PUBLISHED` | Notify driver that the daily queue is published |
| `TAXI_TWO_AHEAD` | Two taxis ahead |
| `TAXI_ONE_AHEAD` | One taxi ahead |
| `TAXI_CALLED_TO_QUEUE` | Driver should join the loading queue |
| `TAXI_SKIPPED` | Taxi was skipped and moved to the back |
| `QUEUE_CANCELLED` | Queue cancelled |
| `QUEUE_REVISED` | Queue revised |

> Message language, character limits, Unicode handling, concatenated SMS behaviour and per-event send policy require confirmation (O02, O03, O15).

### Authentication Provider

- The application validates access tokens using the selected provider's supported protocol.
- The security adapter maps the authenticated subject to an internal user.
- Application services receive a framework-independent `CurrentUser` / actor context through an application port.
- Role and rank-route authorisation is performed for every protected use case.
- Token issuer, audience, expiry, key rotation, account recovery and onboarding remain open until the identity provider is selected (O11).

### Future Push Notifications

Push notifications are explicitly out of scope for Phase 1. The application notification model and event types must remain channel-neutral so a future push adapter can be added without changing queue-domain behaviour.

---

## Non-Functional Requirements

### Security and Privacy

1. Enforce HTTPS for all browser/API traffic.
2. Apply role-based and rank/route-scoped authorisation on the backend.
3. Never rely on frontend route hiding as an authorisation control.
4. Encrypt sensitive personal information at rest using an approved key-management approach.
5. Mask South African ID numbers in user interfaces and API responses.
6. Never log full ID numbers, access tokens, passwords, provider credentials or complete sensitive payloads.
7. Store secrets outside source control and container images.
8. Apply least-privilege access to database, hosting and SMS provider credentials.
9. Protect SMS callback endpoints with provider-supported authentication/signature validation and replay protection.
10. Apply rate limiting to authentication, driver lookup and SMS-triggering operations.
11. Define data retention, access logging, correction and deletion processes in line with POPIA and legal review (O14).
12. A mathematically valid South African ID number is not proof that the person owns that identity.

### Performance

The following are proposed initial targets and must be validated against expected rank volumes:

| Operation | Target |
|---|---|
| Normal queue read | p95 under 2 seconds |
| Queue action (skip/depart) | p95 under 2 seconds, excluding SMS delivery |
| Search taxis by registration | p95 under 2 seconds |
| Queue publication transaction | p95 under 3 seconds for an agreed maximum queue size |
| SMS dispatch | Asynchronous; provider-dependent |
| Driver queue view | p95 under 2 seconds under expected load |

### Reliability

- Database transactions protect queue and audit consistency.
- Transactional outbox prevents notification work from being lost after a successful queue mutation.
- SMS failure must not roll back a committed queue action.
- Failed SMS attempts remain visible and retry according to policy.
- The service exposes liveness and readiness separately.
- Liveness must not fail solely because the SMS provider is temporarily unavailable.
- Readiness should reflect mandatory dependencies such as the database.
- Backups and restore procedures must be defined and tested.

### Availability and Connectivity

- Rank operations require a working connection to the hosted application in Phase 1.
- Driver SMS receipt does not require mobile data, but requires cellular SMS coverage.
- The web interface should handle temporary API failures clearly and must not imply that an unsaved action succeeded.
- Offline queue mutation and later synchronisation are not included in this design.

### Observability

- Every request has a correlation ID.
- Queue actions log actor, rank, route, queue, entry and outcome identifiers.
- Notification logs include event key, provider, attempt count and status.
- Metrics include queue action counts, SMS submission/failure rates, outbox backlog, retry count, API latency and authentication failures.
- Logs must exclude sensitive ID data, secrets and unnecessary SMS content.
- Operational alerts should cover growing outbox backlog, sustained provider failures, database connectivity and repeated authorisation failures.

---

## Error Handling

### API Error Contract

```json
{
  "type": "https://api.example.co.za/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "instance": "/api/v1/taxis",
  "code": "VALIDATION_ERROR",
  "correlationId": "..."
}
```

> The final contract should follow RFC 9457 Problem Details or an agreed equivalent.

### HTTP Status Mapping

| HTTP status | Meaning | Example |
|---|---|---|
| 400 | Invalid request | Malformed registration number or invalid queue order |
| 401 | Unauthenticated | Missing, invalid or expired token |
| 403 | Authenticated but not authorised | Marshal attempts to manage an unassigned route |
| 404 | Resource not found | Queue or taxi does not exist or is not visible to caller |
| 409 | State or concurrency conflict | Taxi already assigned to another active route at this rank; stale queue version |
| 422 | Valid syntax but invalid business operation, if adopted | Attempt to depart an already departed taxi |
| 429 | Rate limit exceeded | Excessive SMS-triggering requests |
| 500 | Unexpected internal error | Unhandled application failure |
| 502/503 | External dependency unavailable | SMS provider unavailable where synchronous provider interaction is exposed |

> Do not disclose internal exception messages, SQL statements, stack traces, provider credentials or personal data to callers.

### Domain Error Examples

- `TaxiAlreadyRegisteredException`
- `TaxiAlreadyAssignedAtRankException`
- `TaxiNotAssignedToRankRouteException`
- `RankAccessDeniedException`
- `MarshalNotAssignedToRankRouteException`
- `QueueAlreadyPublishedException`
- `QueueNotFoundException`
- `QueueEntryNotFoundException`
- `QueueEntryAlreadyDepartedException`
- `InvalidQueueTransitionException`
- `DuplicateQueueEntryException`
- `StaleQueueVersionException`
- `DriverNotAssociatedWithTaxiException`

Map domain/application errors centrally in the REST adapter.

---

## Testing

### Domain Unit Tests

Must run without Quarkus, CDI, PostgreSQL or external services.

**Minimum scenarios:**

1. Normalise vehicle registration consistently.
2. Reject duplicate normalised registration.
3. Permit the same taxi to be assigned to different ranks.
4. Reject a second active route assignment for the same taxi at the same rank.
5. Preserve historical assignments when an assignment ends.
6. Reject a taxi that is not assigned to a rank-route when adding it to a queue.
7. Reject duplicate taxi entries in one queue.
8. Publish a valid draft queue.
9. Reject invalid queue lifecycle transitions.
10. Mark a waiting taxi departed and prevent a second departure.
11. Skip a taxi and move it behind all other waiting taxis.
12. Preserve the relative order of other waiting taxis after a skip.
13. Restore a skipped taxi to `WAITING` when it becomes eligible for another turn, according to the agreed rule.
14. Preserve the immutable skip event after status restoration.
15. Ensure a queue action at one rank does not mutate another rank's queue.
16. Ensure `TAXI_CALLED_TO_QUEUE` is an event and not a queue-entry status.

### Application Use Case Tests

Use mocked output ports to test:

- Rank and route authorisation.
- Taxi onboarding orchestration.
- Route assignment orchestration.
- Queue creation from eligible taxi assignments.
- Queue publication and outbox creation.
- Skip/depart transactions and audit events.
- Driver queue visibility across associated taxis and ranks.
- Notification recipient resolution.
- Duplicate event prevention.
- Retry scheduling and permanent failure handling.

### Adapter Integration Tests

Use Testcontainers with PostgreSQL for:

- Flyway migration from an empty database.
- Unique registration constraint.
- One active taxi route per rank constraint.
- Queue-entry uniqueness.
- Queue order updates under transaction.
- Optimistic locking / concurrent marshal actions.
- Outbox claim behaviour with multiple processors.
- Notification deduplication.
- Persistence mapping between domain models and JPA entities.

Use provider contract tests or a stub HTTP server for SMS adapter behaviour. Do not make ordinary automated tests send real SMS messages.

### API Tests

Use REST Assured or equivalent to verify:

- Authentication and authorisation.
- Role and rank-route scope enforcement.
- Request validation and error contracts.
- Queue creation, publication, skip and depart endpoints.
- Driver visibility boundaries.
- Idempotency and stale-version responses.

### Architecture Tests

Enforce:

- Domain packages have no Quarkus, Jakarta, Hibernate, Panache, REST, Jackson or provider SDK dependencies.
- Application packages do not depend on adapter implementations.
- Inbound adapters call input ports rather than repositories.
- Outbound adapters implement application-owned ports.
- No JPA entity is returned by a REST resource.
- No SMS provider type leaks beyond its adapter.
- Constructor injection is used for application services.
- Domain aggregates do not expose public setters that permit invariant bypass.

---

## Deployment and Operations

### Runtime

- Deploy the Quarkus backend as a container.
- Deploy the React web application as a static web application or through an approved web-hosting service.
- Use managed PostgreSQL or an approved PostgreSQL hosting option.
- Keep SMS provider credentials in a secret manager.
- Use environment-specific configuration without embedding secrets in source control.
- Run Flyway migrations as a controlled deployment step.
- Use JVM mode initially; native compilation is not required for Phase 1.
- The exact Azure hosting products, network topology, environments and infrastructure-as-code approach remain deployment decisions.

### Environments

| Environment | Purpose |
|---|---|
| Development | Local developer and feature branch testing |
| Test / Integration | Automated tests and integration verification |
| UAT | Business acceptance and stakeholder sign-off |
| Production | Live operations |

> Production SMS credentials and sender identity must never be used in development or automated tests. Provide a test adapter or provider sandbox where available.

### Health Endpoints

- **Liveness:** confirms the process is alive.
- **Readiness:** confirms mandatory dependencies such as the database are available.
- SMS provider availability should be observable but should not automatically cause the whole application to become unready if queue management can continue and SMS events can safely remain in the outbox.

### Backup and Recovery

Define and test:

- PostgreSQL backup frequency and retention.
- Point-in-time recovery where supported.
- Restore procedure and recovery objectives.
- Outbox recovery after restart.
- Behaviour when an SMS provider is unavailable for an extended period.
- Operational reconciliation of notifications with unknown provider outcomes.

---

## Appendices

### A. Role and Permission Matrix

| Capability | RANK_ADMIN | QUEUE_MARSHAL | DRIVER |
|---|---|---|---|
| View assigned ranks | Yes | Assigned rank context | No |
| Switch between assigned ranks | Yes | Only assigned route context | No |
| Create/update rank | Yes, assigned scope | No | No |
| Create/update routes | Yes, assigned rank | No | No |
| Onboard drivers | Yes | No | No |
| Register taxis | Yes | No | No |
| Assign taxi to rank-route | Yes | No | No |
| Onboard marshals | Yes | No | No |
| Assign marshals to routes | Yes | No | No |
| Search eligible taxis for queue | Yes | Assigned routes | No |
| Create and order queue | Yes | Assigned routes | No |
| Publish queue | Yes | Assigned routes | No |
| Mark taxi departed | Yes | Assigned routes | No |
| Skip taxi | Yes | Assigned routes | No |
| View queue history | Yes, assigned ranks | Assigned routes | Own associated taxis only |
| View own queue positions | If also a driver under approved multi-role model | If also a driver under approved multi-role model | Yes |
| View notification delivery status | Assigned rank scope | Assigned route scope if authorised | Own notifications only |

> The table describes the intended role capabilities. Whether one user may hold multiple roles is an open decision (O04, O05).

### B. Queue Entry Status Reference

| Status | Meaning | Terminal? |
|---|---|---|
| `WAITING` | Taxi is waiting for its turn | No |
| `SKIPPED` | Taxi was skipped and repositioned behind other waiting taxis | No |
| `DEPARTED` | Taxi has loaded passengers and left | Yes |

> No `CALLED`, `BOARDING`, `COMPLETED` or other queue-entry status is introduced in Phase 1 without an approved business change. Queue lifecycle state is a separate concept from queue-entry status.

### C. South African Identifier Handling

**South African ID number**

- Validate 13-digit structure, encoded date validity and checksum.
- Do not treat a passing checksum as identity verification.
- Restrict access and mask in normal user interfaces.
- Encrypt at rest and use a keyed lookup mechanism if duplicate detection is required.
- Do not log the full value.
- Confirm lawful purpose, retention and access controls with the responsible privacy/legal stakeholder.

**Vehicle registration**

- Accept supported South African provincial, legacy and personalised formats.
- Normalise whitespace and separators for comparison.
- Retain the original display value.
- Avoid a single rigid regex that rejects legitimate historic or provincial variants.
- Make validation rules testable and configurable.

### D. Glossary

| Term | Definition |
|---|---|
| Rank | Physical taxi rank from which taxi services operate |
| Route | Origin-to-destination journey definition |
| Rank-route | A route service operating from a particular rank |
| Taxi assignment | Association of a taxi with one rank-route at one rank for a period |
| Driver | Person associated with one or more taxis and entitled to view their relevant queues |
| Rank administrator | User authorised to manage one or more assigned ranks |
| Queue marshal | User authorised to operate queues for assigned rank-route services |
| Daily queue | Ordered list for a rank-route and service date/session |
| Queue entry | A taxi's participation in one daily queue |
| Queue event | Immutable record of an operational queue action |
| Outbox | Database-backed record of work to be processed after the business transaction commits |
| SMS gateway | External provider used to submit SMS messages |
| Hexagonal architecture | Ports-and-adapters architecture that keeps application/domain logic independent of external technologies |
| LLD | Low-Level Design |
| POPIA | Protection of Personal Information Act (South Africa) |

### E. Implementation Readiness Checklist

The following must be completed before feature implementation begins:

- [ ] Product owner resolves the open business decisions in this document.
- [ ] Queue lifecycle and revision rules are approved.
- [ ] SMS event and recipient policy is approved.
- [ ] Driver onboarding and mobile-number verification flow is approved.
- [ ] Driver-to-taxi relationship cardinality is approved.
- [ ] Identity provider and token contract are selected.
- [ ] SMS provider is selected and its delivery/callback contract is documented.
- [ ] Data retention and POPIA controls are reviewed.
- [ ] OpenAPI contracts are reviewed and approved.
- [ ] ERD and database constraints are reviewed.
- [ ] Hexagonal module boundaries and dependency rules are approved.
- [ ] Transaction, concurrency and outbox strategy is reviewed.
- [ ] Deployment target and environment strategy are approved.

### F. References

- Product and workflow decisions captured in the Taxi Rank Manager design discussions.
- Reference LLD: `example-lld.md` (Auto Recommendations – Hearing Recommendation LLD). Its section structure and level of implementation detail were used as the document-format example.
- Quarkus and library versions must be selected and pinned during project bootstrap; this LLD intentionally does not invent patch versions.
