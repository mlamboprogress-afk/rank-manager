# Taxi Rank Manager — Claude Code Instructions

## Status file

`STATUS.md` in the project root tracks what has been built and what is outstanding.

**Update `STATUS.md` after every change session.** Specifically:

- When a feature, service, adapter, entity, or test is completed, move it from the outstanding list to the built list (or add a new checked item).
- When new outstanding work is identified, add it to the relevant outstanding section.
- When the build state changes (new module, test count changes, compile status changes), update the **Build verification** section at the bottom.
- Keep the `> Last updated:` date at the top current.

Do not rewrite the whole file unless the structure needs to change. Prefer targeted edits: tick a checkbox, add a line, update a date.

---

## Project layout

```
C:\development\rank-manager\
├── modules/          # Domain modules (pure Java — no Quarkus/Jakarta in domain packages)
│   ├── shared-kernel
│   ├── identity
│   ├── rank
│   ├── fleet
│   ├── route
│   ├── queue
│   └── notification
├── adapters/         # Hexagonal adapters (Quarkus CDI allowed here)
│   ├── inbound-rest
│   ├── inbound-scheduler
│   ├── outbound-postgres
│   ├── outbound-sms
│   ├── outbound-security
│   └── outbound-clock
├── apps/
│   └── taxi-rank-manager-service   # Quarkus runnable application
├── frontend/
│   └── taxi-rank-manager-web       # React + TypeScript + Vite
├── docs/             # LLD and design documents
└── libraries/        # Project-local JDK and Maven binaries
```

## Maven

**Never use `mvn` or `java` directly** — they are not on PATH.
Always run:

```
mvnw.cmd -f C:\development\rank-manager\pom.xml <goals>
```

For module-scoped builds:

```
mvnw.cmd -f C:\development\rank-manager\pom.xml <goals> -pl <module-path> -am
```

## Architecture rules (enforced by ArchUnit)

- `..domain..` packages must not import Quarkus, Jakarta, Hibernate, Panache, Jackson, or SMS SDK types.
- `..application.service..` packages may use `@ApplicationScoped` and `@Inject` (CDI annotations are acceptable outside the domain package).
- `@Transactional` belongs on inbound adapter methods (REST resources), not on application services.
- JPA entities live only in `adapter-outbound-postgres` — never in domain modules.
- Adapters must not import other adapters directly.

## Base package

`za.co.taxirankmanager`

## Key design decisions

- **Optimistic locking** via `@Version` on JPA entities and `expectedVersion` on write commands.
- **`QueueEntryStatus`** has exactly three values: `WAITING`, `SKIPPED`, `DEPARTED`. No `CALLED`.
- **`SouthAfricanIdNumber.toString()`** always returns the masked value. Use `.getValue()` only when the raw value is explicitly needed.
- **`VehicleRegistration`** equality is normalised (strip non-alphanumeric, uppercase).
- **Transactional outbox** — `notification_outbox` rows are written in the same transaction as the queue operation. The scheduler processes them asynchronously.
- **Driver ID storage** — `id_number_hash` (SHA-256) for lookup; `id_number_enc` for the raw value (pending AES encryption before production).
