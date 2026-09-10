# Rental Property & Tenant Management System

A Spring Boot backend for landlords to track properties, tenants, rent payments,
and maintenance requests — with JWT auth, role-based access control, and
scheduled jobs for overdue-rent detection and lease-expiry reminders.

## Tech stack

- Java 17, Spring Boot 3.3.4
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- JWT (jjwt 0.11.5)
- Flyway (schema migrations) — H2 (in-memory, zero-setup default) / MySQL (production profile)
- springdoc-openapi (Swagger UI)
- Lombok

## Running it

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080` using an in-memory H2 database.
Flyway runs `src/main/resources/db/migration/V1__init_schema.sql` on startup
to create the schema, then a demo-data seeder populates it — no manual setup
needed. Data resets every restart.

**Demo login (seeded automatically on every dev/H2 startup):**
- Owner: `alice@owner.com` / `password123`
- Tenant: `bob@tenant.com` / `password123`

The seed includes 2 properties (one occupied, one vacant), a tenant with a
lease expiring in 20 days (inside the 30-day reminder window), one paid rent
payment, one already-overdue pending payment, one future pending payment,
and one open maintenance request. Seeding is skipped automatically if data
already exists, and is disabled entirely on the `mysql` and `test` profiles.

**H2 console** (handy for peeking at tables while developing):
`http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:rentaldb`, user `sa`, no password.

**Swagger UI** (browse and test every endpoint, including JWT auth via the
"Authorize" button — paste in a token from `/api/auth/login`):
`http://localhost:8080/swagger-ui.html`

### Switching to MySQL

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Configure via env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.
See `src/main/resources/application-mysql.properties`.

### JWT secret

Set your own via env var before deploying anywhere real:

```bash
export JWT_SECRET="a-long-random-string-at-least-32-characters"
```

## Project structure

```
entity/        JPA entities (Owner, Property, Tenant, RentPayment, MaintenanceRequest)
enums/         Role, PropertyType, PaymentStatus, MaintenanceStatus, Priority
repository/    Spring Data JPA repositories
dto/request/   Request payloads (validated with Bean Validation)
dto/response/  Response payloads (never expose entities directly)
security/      JWT filter, JWT util, UserDetailsService, security-context helper
config/        SecurityConfig (role-based route rules, CORS, password encoder)
service/       Business logic + access-control checks
controller/    REST endpoints
scheduler/     @Scheduled jobs (overdue rent, lease expiry)
exception/     Custom exceptions + @RestControllerAdvice global handler
```

## Design notes (read before extending)

- **Owner and Tenant are separate entities/tables**, both authenticate via the
  same `/api/auth/**` endpoints. `CustomUserDetailsService` checks the Owner
  table first, then Tenant, by email. `AppUserPrincipal` is the unified
  Spring Security principal wrapping either one.
- **Property → Tenant is modeled one-to-many** (not one-to-one) so multi-unit
  properties or tenant history can be added later without a schema change.
  Today's "one active tenant per property" rule is enforced in
  `PropertyService.assignTenant` (rejects if `isOccupied` is already true or
  the tenant already has a property), not in the schema.
- **Maintenance-request ownership check happens server-side**, in
  `MaintenanceRequestService.createRequest` — a tenant can only raise a
  request for the property they're actually assigned to, regardless of what
  `propertyId` the client sends.
- **RentPayment.status transitions are one-directional for the scheduler**:
  `OverdueRentScheduler` only ever moves `PENDING -> OVERDUE`. It never
  touches `PAID` payments, so a late-but-settled payment is never
  re-flagged. `markPaid` is the only path that sets `PAID`.
- **Lease-expiry job currently only logs.** `LeaseExpiryScheduler` is the
  extension point for wiring in real notifications (email/SMS/in-app) —
  see the TODO in that class.
- **Schema is owned by Flyway, not Hibernate.** `ddl-auto=validate` means
  Hibernate only checks entity mappings match the migrated schema — it never
  creates or alters tables. Add new migrations as `V2__*.sql`,
  `V3__*.sql`, etc. under `src/main/resources/db/migration`; never edit
  `V1__init_schema.sql` once it's been run anywhere.
- **Tests use a separate H2 instance** (`rentaldb_test`, see
  `application-test.properties`) and each test method runs inside a
  transaction that's rolled back afterwards (`@Transactional` on
  `AbstractIntegrationTest`), so tests never leak data into each other and
  you can reuse the same demo emails freely across test methods.

## API reference

### Auth
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | public — body includes `role: OWNER \| TENANT` |
| POST | `/api/auth/login` | public — returns JWT |

### Properties (Owner only)
| Method | Endpoint |
|---|---|
| POST | `/api/properties` |
| GET | `/api/properties` |
| PUT | `/api/properties/{id}` |
| DELETE | `/api/properties/{id}` |
| POST | `/api/properties/{id}/assign-tenant/{tenantId}` |

### Tenants
| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/tenants/{id}` | Owner (any), Tenant (self only) |
| GET | `/api/tenants/{id}/property` | Owner (any), Tenant (self only) |

### Rent Payments
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/rent-payments` | Owner |
| GET | `/api/rent-payments/tenant/{tenantId}` | Owner (own properties), Tenant (self) |
| GET | `/api/rent-payments/overdue` | Owner |
| PUT | `/api/rent-payments/{id}/mark-paid` | Owner |

### Maintenance Requests
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/maintenance-requests` | Tenant |
| GET | `/api/maintenance-requests/property/{propertyId}` | Owner |
| GET | `/api/maintenance-requests/my-requests` | Tenant |
| PUT | `/api/maintenance-requests/{id}/status` | Owner |

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

## Running the tests

```bash
mvn test
```

Covers: register/login/duplicate-email/bad-password, owner-only property
CRUD with cross-owner access denial, tenant-can't-raise-request-for-
unassigned-property, full maintenance status lifecycle including the
already-resolved guard, cross-tenant and cross-owner rent-payment access
denial, and the overdue scheduler's flag/exclude logic (invoked directly
rather than waiting for its cron trigger).

## Suggested next steps

1. `mvn spring-boot:run`, then open `http://localhost:8080/swagger-ui.html`
   and try the seeded demo accounts end to end.
2. `mvn test` to confirm everything passes in your environment (this was
   built without Maven Central access on my end, so a local run is the real
   verification).
3. Wire `LeaseExpiryScheduler` to a real notification channel.
4. Build the React dashboard (Owner view / Tenant view) against this API.
