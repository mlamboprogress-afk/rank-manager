# Taxi Rank Manager

## Business Context, Problem Statement and Proposed Solution

---

## 1. Project Overview

Taxi Rank Manager is a digital taxi queue management platform designed specifically for the South African minibus taxi industry.

The purpose of the platform is to replace manual, paper-based taxi queue management with a centralised digital system that allows taxi rank administrators and queue marshals to manage taxi queues efficiently, transparently and consistently.

The platform will allow taxi drivers to register their vehicles, associate themselves with taxi ranks and routes, and receive SMS notifications about their position in a queue and when they need to prepare to load passengers.

The application will initially be developed as a responsive web application. It must support desktop computers, tablets and mobile phones.

SMS will be the primary notification channel in the first phase. Drivers must not be required to install a mobile application or have mobile data to receive queue notifications.

The system should be designed to support multiple taxi ranks, routes, administrators, queue marshals and drivers.

---

## 2. The Problem

Many South African taxi ranks still rely on manual processes to manage the order in which taxis load passengers.

Queue marshals typically use handwritten lists, notebooks, verbal communication or other informal methods to keep track of which taxi should load next.

These processes create several operational challenges:

### 2.1 Manual queue management

Queue marshals must manually maintain the order of taxis waiting to load passengers.

This can become difficult when:

- There are many taxis waiting to load.
- Taxis arrive or leave throughout the day.
- A taxi is skipped because its driver is unavailable.
- A taxi needs to be moved to the back of the queue.
- Multiple marshals are managing the same route.
- A queue needs to be restarted or changed.

### 2.2 Lack of transparency

Drivers may not have a reliable way of knowing:

- Their current position in the queue.
- How many taxis are ahead of them.
- Whether their position has changed.
- Whether they have been skipped.
- When they are expected to move closer to the loading area.

This can lead to confusion, unnecessary waiting and disagreements about queue order.

### 2.3 Communication challenges

Queue marshals often have to communicate directly with drivers to inform them that their turn is approaching.

Drivers may not be close enough to hear announcements or may not be physically present when their turn arrives.

This creates delays and makes it difficult to manage the queue efficiently.

### 2.4 Lack of operational records

Paper-based queue management makes it difficult to maintain reliable historical records.

There may be no consistent record of:

- Which taxis participated in a queue.
- Which taxi loaded passengers.
- Which taxis were skipped.
- Who changed the queue order.
- When a queue was published or cancelled.
- Which marshal performed a particular action.

### 2.5 Multiple taxi ranks and routes

A taxi may operate at different taxi ranks and may serve different routes depending on the rank where it is operating.

The system must therefore distinguish between a taxi's participation at one taxi rank and its participation at another.

Queue progress at one taxi rank must never affect the same taxi's queue position at another taxi rank.

---

## 3. Proposed Solution

Develop a centralised Taxi Rank Manager platform that digitises the registration, allocation and queue management processes at taxi ranks.

The platform will provide three main capabilities:

1. **Taxi rank administration:** Register and manage taxi ranks, routes, drivers, taxis and queue marshals.
2. **Digital queue management:** Allow authorised queue marshals to create, publish and manage daily taxi queues.
3. **Driver communication:** Give drivers visibility of their queue information and send SMS notifications when their turn approaches.

The platform will maintain a reliable record of queue activity and ensure that all queue operations follow clearly defined business rules.

### 3.1 Digital taxi registration and allocation

Taxi rank administrators will be able to register drivers and taxis.

Each taxi will have:

- A unique South African vehicle registration number.
- One or more associated drivers, subject to the final driver-assignment rules.
- Associations with one or more taxi ranks.
- Exactly one assigned route at each individual taxi rank.

A taxi can therefore operate at multiple ranks while having a different route assignment at each rank.

**Example:**

| Taxi | Taxi rank | Assigned route |
|---|---|---|
| ABC123MP | Witbank | Witbank to Pretoria |
| ABC123MP | Pretoria | Pretoria to Witbank |

The same taxi must not be assigned to two different routes at the same taxi rank simultaneously.

### 3.2 Digital queue creation

Queue marshals will be able to create a daily queue for a specific rank and route.

The proposed process is:

1. The marshal logs into the platform.
2. The marshal selects an assigned taxi rank and route.
3. The system retrieves the taxis allocated to that rank and route.
4. The marshal can search for taxis using their registration number or driver details.
5. The marshal reviews the taxis included in the queue.
6. The marshal can arrange their order.
7. The marshal publishes the queue.

Once published, the queue becomes the active operational queue for that rank and route.

The system must record who created and published the queue and when these actions occurred.

### 3.3 Queue management

Queue marshals will manage the queue through the application.

The system must support the following operations:

- View the current queue.
- View taxi registration details and associated driver information.
- Search for taxis.
- Skip a taxi.
- Move a skipped taxi to the back of the waiting queue.
- Mark a taxi as departed after it has loaded passengers and left.
- View the history of queue operations.

Queue operations must update the queue consistently and be recorded for auditing purposes.

### 3.4 Driver queue visibility

Drivers will be able to log into the web application and view queue information for all taxis associated with their driver profile.

Drivers must be able to see:

- The taxi registration number.
- The relevant taxi rank and route.
- Their taxi's current queue status.
- Their taxi's position in the queue.
- The number of taxis ahead of them.
- Relevant queue notifications.

Drivers will have read-only access to queue information.

They must not be able to change queue order, skip taxis, mark taxis as departed or perform marshal operations.

### 3.5 SMS notifications

SMS will be the primary notification channel in Phase 1.

Drivers should receive relevant notifications about changes in their queue position and when they need to prepare for their turn.

Proposed notification events include:

- Daily queue published.
- Two taxis ahead.
- One taxi ahead.
- Taxi called to the loading area.
- Taxi skipped.
- Queue revised or cancelled.

The exact notification thresholds and recipient rules must be configurable or explicitly defined before implementation.

SMS must be sent asynchronously so that a failure at the SMS provider does not prevent a marshal from successfully performing a queue operation.

The system must maintain notification records, delivery attempts and provider responses where available.

The application must use an SMS provider-independent design so that the provider can be changed without modifying queue management business logic.

---

## 4. Application User Roles

The application will have exactly three business roles in Phase 1.

### 4.1 RANK_ADMIN

A rank administrator is responsible for managing the operational configuration of one or more taxi ranks.

Responsibilities include:

- Manage assigned taxi ranks.
- Register drivers.
- Register taxis.
- Register queue marshals.
- Create and manage routes.
- Assign taxis to routes at specific ranks.
- Allocate queue marshals to rank-route combinations.
- View and manage queues.
- Perform all queue marshal operations.
- View queue history and operational records.

A rank administrator may administer multiple taxi ranks and must be able to switch between them after logging in.

### 4.2 QUEUE_MARSHAL

A queue marshal is responsible for managing queues for the rank-route combinations assigned to them.

Responsibilities include:

- View assigned taxi ranks and routes.
- Create daily queues.
- Search and select eligible taxis.
- Arrange queue order.
- Publish queues.
- Skip taxis.
- Mark taxis as departed.
- View queue activity.

A queue marshal must only be able to manage the rank-route combinations to which they have been assigned.

A marshal must not be able to manage another rank-route unless explicitly allocated to it.

### 4.3 DRIVER

A driver is a registered taxi driver associated with one or more taxis.

Responsibilities and permissions include:

- Log into the application.
- View taxis associated with their driver profile.
- View queue information for those taxis across different taxi ranks.
- Receive SMS notifications.
- View relevant queue status and position information.

Drivers have read-only queue access.

---

## 5. Core Business Rules

These rules are fundamental to the application and must be enforced by the backend, not only by the frontend.

### 5.1 Taxi registration

- Every taxi must have a unique South African vehicle registration number.
- Vehicle registration numbers must be normalised before uniqueness checks.
- The system must accommodate South African registration formats from different provinces and legacy formats.
- A taxi can be associated with multiple taxi ranks.

### 5.2 Driver registration

- Every driver must have a valid South African ID number.
- Driver identity information must be protected.
- ID numbers must not be exposed unnecessarily in API responses, logs or frontend displays.
- Mathematical ID validation must not be treated as proof of identity.

### 5.3 Taxi-to-route assignment

A taxi may have multiple rank associations, but only one active route assignment per taxi at a particular rank.

**Valid:**
- Taxi ABC123MP at Witbank: Witbank to Pretoria.
- Taxi ABC123MP at Pretoria: Pretoria to Witbank.

**Invalid:**
- Taxi ABC123MP at Witbank: Witbank to Pretoria.
- Taxi ABC123MP at Witbank: Witbank to Johannesburg.

The database and application logic must enforce this rule.

### 5.4 Queue independence

Each queue belongs to a specific:

- Taxi rank.
- Route.
- Service date.

A taxi may participate in queues at different ranks independently.

If ABC123MP is number five in the Witbank queue, its position in the Pretoria queue must remain unaffected by any operation performed at Witbank.

### 5.5 Queue entry statuses

There are exactly three queue-entry statuses:

**WAITING**
- The default status when a taxi enters a queue.
- The taxi is waiting for its turn.

**SKIPPED**
- The taxi has been skipped by a marshal.
- It must be moved to the back of the waiting order.
- The original skip action must remain in the audit history.

**DEPARTED**
- The taxi has loaded passengers and left the taxi rank.
- This is a terminal status for that queue entry.
- A departed entry cannot be skipped or departed again.

There must not be a `CALLED` queue-entry status. Calling a taxi is a notification or operational event, not a queue-entry status.

### 5.6 Skip operation

When a marshal skips a taxi:

- The skip action must be recorded.
- The taxi must move to the back of the waiting queue.
- Other waiting taxis must retain their relative order.
- The system must preserve the original skip event in its audit history.
- The taxi must be able to participate in the queue again according to the defined queue progression rules.

The exact point at which a `SKIPPED` entry returns to `WAITING` must be explicitly defined during detailed design.

### 5.7 Queue integrity

- A taxi must not appear more than once in the same queue.
- Queue ordering must be deterministic.
- Queue operations must be transactional.
- Every important queue mutation must record the actor and timestamp.
- Concurrent marshal operations must not silently overwrite one another.
- Stale queue updates must be detected and rejected with an appropriate conflict response.
- Queue history must be preserved when a queue is revised.

---

## 6. Queue Lifecycle

Queue lifecycle and queue-entry status must be treated as separate concepts.

A queue represents an operational session for a particular rank-route and service date.

A queue entry represents an individual taxi's participation in that queue.

The proposed queue lifecycle includes:

| Status | Meaning |
|---|---|
| `DRAFT` | Queue is being prepared |
| `PUBLISHED` | Queue is active and available for operational management |
| `CANCELLED` | Queue has been cancelled |
| `COMPLETED` | Queue has finished its operational lifecycle |

These are queue lifecycle states, not queue-entry statuses.

The application must preserve queue revisions and record the actor, timestamp and reason for each revision.

Whether a rank-route can have multiple queue sessions on the same day remains an open business decision.

---

## 7. Operational Scope

The system must support multiple taxi ranks within the same application.

The following relationships must be supported:

- One rank can have multiple routes.
- One rank can have multiple administrators.
- One rank-route can have multiple queue marshals.
- One taxi can be associated with multiple ranks.
- One taxi has one active route assignment per rank.
- One driver can be associated with multiple taxis, subject to the final driver-assignment rules.
- A driver can view queues for all associated taxis.
- A marshal can only manage assigned rank-route combinations.
- An administrator can manage multiple assigned ranks.

The application must enforce access control at both role and resource-scope level.

Having the `QUEUE_MARSHAL` role alone must not grant access to every rank or route.

---

## 8. Proposed Technical Architecture

The application should be implemented as a modular monolith using Hexagonal Architecture (Ports and Adapters).

### Backend

- Java 21
- Quarkus
- Maven
- Quarkus REST
- Jackson
- Hibernate ORM/JPA
- PostgreSQL
- Flyway
- Quarkus Security
- OpenAPI / SmallRye OpenAPI

### Frontend

- React
- TypeScript
- Responsive web interface

### Infrastructure and supporting tools

- Docker
- Git
- GitHub Actions
- Azure-compatible deployment architecture
- SMS provider integration

### Testing

- JUnit 5
- Mockito
- REST Assured
- Testcontainers
- Architecture dependency tests using ArchUnit or an equivalent approach

### Hexagonal architecture rules

The architecture must enforce the following:

1. The domain layer must not depend on Quarkus, Jakarta, Hibernate, Panache, REST, Jackson or external SMS SDKs.
2. Application use cases must contain orchestration and business workflows.
3. Input ports must be owned by the application layer.
4. Output ports must be owned by the application layer.
5. Inbound adapters must call application input ports.
6. Outbound adapters must implement application output ports.
7. REST controllers must never access repositories directly.
8. Domain aggregates must not be ORM entities.
9. REST DTOs, domain models and persistence entities must be separate.
10. Database transactions must be controlled at the application use-case boundary.

The backend must remain a single deployable application in Phase 1. Microservices are not required.

---

## 9. Important Technical Requirements

### 9.1 Transactional queue operations

When a queue operation occurs, the following must be committed atomically:

- Queue state changes.
- Queue audit event.
- Notification outbox record, where a notification is required.

SMS delivery must happen asynchronously after the transaction commits.

An SMS provider failure must never roll back a successfully committed queue operation.

### 9.2 Transactional outbox

Implement a transactional outbox for reliable notification processing.

The system must:

- Persist notification events alongside queue changes.
- Process pending notifications asynchronously.
- Prevent duplicate processing where possible.
- Retry transient failures using bounded retry policies.
- Persist delivery attempts and provider references.
- Support delivery status updates through provider callbacks where available.

### 9.3 Concurrency

Multiple marshals may work on the same queue.

The system must prevent lost updates and inconsistent ordering.

Use optimistic locking, row-level locking or an appropriate combination.

If a marshal submits an operation based on an outdated queue version, the system must reject the operation and return a clear conflict response.

### 9.4 Security and privacy

- Enforce role-based and resource-level access control.
- Protect South African ID numbers and personal information.
- Mask sensitive data in user interfaces.
- Never log full ID numbers or sensitive authentication information.
- Secure authentication and API access.
- Follow applicable POPIA requirements.
- Maintain an audit history of important administrative and queue operations.

### 9.5 Internet connectivity

Phase 1 is an online web application.

Queue mutations require connectivity to the backend. Offline queue mutation and synchronisation are not part of the initial scope.

A manual operational fallback should be considered for situations where the taxi rank loses internet connectivity.

---

## 10. Out of Scope for Phase 1

The following are not required for the initial release:

- Native Android application.
- Native iOS application.
- Push notifications.
- Microservices architecture.
- Kubernetes.
- Online passenger bookings.
- Passenger payments.
- Automated taxi fare collection.
- GPS tracking.
- Driver location tracking.
- Automated route optimisation.
- Offline-first queue management.
- Advanced analytics and business intelligence.

The architecture should allow future extensions without requiring major changes to the core queue domain.

---

## 11. Business Objectives and Expected Benefits

The platform aims to deliver the following benefits:

1. Replace manual paper-based queue management with a digital process.
2. Improve transparency of taxi queue positions.
3. Reduce confusion and disputes around queue ordering.
4. Improve communication between queue marshals and drivers.
5. Reduce the need for marshals to repeatedly locate and verbally notify drivers.
6. Maintain a reliable historical record of queue operations.
7. Support multiple taxi ranks through one centralised platform.
8. Provide a foundation for future operational reporting and management.

The system should be simple enough for taxi rank staff and drivers to use with minimal training.

---

## 12. Open Business Decisions

The following items must not be silently assumed during implementation.

These are unresolved requirements that require explicit clarification or recorded assumptions before the corresponding features are finalised.

### Queue operations

1. Can a rank-route have more than one queue session per day?
2. When a queue is published, are all drivers expected to physically line up immediately, or should drivers only be called when their turn approaches?
3. Does queue position represent absolute list position or position among `WAITING` taxis only?
4. At what point does a `SKIPPED` taxi return to `WAITING`?
5. Can a taxi that has departed participate in another queue session on the same day?
6. Can a marshal add a taxi after the queue has been published?
7. What happens when a taxi is registered to a rank-route but is unavailable on a particular day?

### Driver and taxi relationships

8. Can a driver be associated with multiple taxis?
9. Can multiple drivers be associated with one taxi?
10. Can multiple drivers be actively associated with the same taxi simultaneously?
11. How are driver mobile numbers verified?
12. Can a driver update their own contact details?

### SMS notifications

13. Which events must generate SMS messages?
14. Should every driver receive a notification when a queue is published?
15. Should the two-ahead and one-ahead notifications be sent automatically?
16. What should happen when a driver does not respond to a called-to-queue SMS?
17. Which SMS languages should be supported?
18. What SMS usage budget or rate limits should apply?

### Administration and identity

19. Can one user have more than one business role?
20. How are the first rank administrators created and authorised?
21. Can administrators manage other administrators, or only their own assigned ranks?
22. What onboarding and account recovery process should be used?

---

## 13. Instructions for Claude Code

Treat this document as the initial business context and source of business requirements for the Taxi Rank Manager project.

Before implementing features:

1. Understand the business problem and operational workflow.
2. Identify the domain entities and their relationships.
3. Understand the distinction between a taxi, a taxi's rank assignment, a route assignment, a queue and a queue entry.
4. Preserve all explicitly stated business rules.
5. Do not introduce additional business roles or queue-entry statuses.
6. Do not treat a taxi's queue position at one rank as related to its position at another rank.
7. Do not implement SMS delivery directly inside queue business logic.
8. Preserve the Hexagonal Architecture dependency rules.
9. Identify unresolved business decisions before making assumptions.
10. Maintain automated tests for business rules, security, queue ordering, concurrency and notification processing.

The priority is to build a reliable, maintainable and extensible taxi rank management platform that accurately reflects how taxi ranks operate in South Africa.

Do not optimise for unnecessary technical complexity. Build a well-structured modular monolith that can support the initial business requirements and grow as the product is adopted by more taxi ranks.

Business correctness, queue integrity, auditability and operational simplicity take priority over introducing additional technologies or architectural complexity.
