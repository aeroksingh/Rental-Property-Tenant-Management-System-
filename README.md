# Ledger: Rental Property & Tenant Management System

A full stack web app for landlords to manage properties, tenants, rent payments, and maintenance requests, with a matching tenant portal on the other side. Spring Boot API backend, React + Vite frontend.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green)
![React](https://img.shields.io/badge/React-18-blue)
![Vite](https://img.shields.io/badge/Vite-5-purple)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## What Is This?

Most landlords track this stuff in a spreadsheet or a group chat: which units are occupied, who owes rent, whose lease is about to expire, who filed a maintenance request last week. This project turns that into a proper two-sided application.

**Owners** get a dashboard to manage properties, assign tenants, record and track rent payments, and resolve maintenance requests. **Tenants** get their own portal to see their unit, their rent history, and raise maintenance requests, with role based access enforced on every request so a tenant can never reach an owner's data (or another tenant's) no matter what they type in the URL.

Two scheduled background jobs run without any manual triggering: one flags rent payments as overdue once they pass their due date, and one watches for leases expiring within 30 days.

## Architecture

```
┌────────────────────────────────────────────┐
│           React + Vite Frontend             │
│   Owner dashboard · Tenant portal           │
│   Tailwind CSS · React Router · Axios       │
└───────────────────┬──────────────────────────┘
                     │ REST + JWT (Bearer token)
┌───────────────────▼──────────────────────────┐
│            Spring Boot Backend :8080          │
│   REST API · Spring Security · Role based ACL  │
│   @Scheduled jobs: overdue rent, lease expiry   │
└───────────────────┬──────────────────────────┘
                     │ Flyway managed schema
┌───────────────────▼──────────────────────────┐
│   H2 (in memory, default) or MySQL (prod)      │
└──────────────────────────────────────────────┘
```

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Backend | Java 17, Spring Boot 3.3.4 | REST API, auth, business logic |
| Security | Spring Security, JWT (jjwt 0.11.5) | Stateless auth, role based access control |
| Database | H2 (dev) / MySQL (prod), Flyway | Schema managed by migrations, not Hibernate DDL |
| Docs | springdoc-openapi (Swagger UI) | Interactive API docs and testing |
| Frontend | React 18, Vite 5 | SPA, fast dev server and build |
| Styling | Tailwind CSS | Utility first styling |
| Routing | React Router 6 | Client side routing, role gated |
| HTTP | Axios | API calls with JWT attached automatically |

## Features

**Owner side**
- Create, update, and delete properties
- Assign a tenant to a vacant property
- Record rent payments and mark them paid
- View overdue rent across all properties
- View and resolve tenant maintenance requests

**Tenant side**
- View their assigned property and lease details
- View their own rent payment history
- Raise a maintenance request for their unit
- Client side lease expiry warning (mirrors the backend's 30-day window)

**Across both**
- JWT based login and registration, with role selection (Owner or Tenant)
- Role gated routing on the frontend and role gated endpoints on the backend, so neither side can reach the other's data by guessing a URL or an ID
- Automatic overdue-rent detection and lease-expiry reminders, run on a schedule with no manual trigger
- Swagger UI for exploring and testing every endpoint directly

## Project Structure

```
Rental-Property-Tenant-Management-System/
├── backend/                          # Spring Boot API
│   ├── src/main/java/.../
│   │   ├── entity/                   # Owner, Property, Tenant, RentPayment, MaintenanceRequest
│   │   ├── enums/                    # Role, PropertyType, PaymentStatus, MaintenanceStatus, Priority
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── dto/request/              # Validated request payloads
│   │   ├── dto/response/             # Response payloads (entities never exposed directly)
│   │   ├── security/                 # JWT filter, JWT util, UserDetailsService
│   │   ├── config/                   # SecurityConfig (routes, CORS, password encoder)
│   │   ├── service/                  # Business logic + access control checks
│   │   ├── controller/               # REST endpoints
│   │   ├── scheduler/                # @Scheduled jobs (overdue rent, lease expiry)
│   │   └── exception/                # Custom exceptions + global handler
│   ├── src/main/resources/db/migration/  # Flyway SQL migrations
│   └── pom.xml
│
└── frontend/                         # React + Vite
    ├── src/
    │   ├── api/                      # One file per backend resource, all via axiosClient.js
    │   ├── context/                  # AuthContext: logged-in user + token
    │   ├── components/                # Button, TextField, Modal, Sidebar, ProtectedRoute, etc.
    │   ├── pages/                    # LoginPage, RegisterPage, OwnerProperties,
    │   │                              # OwnerOverdueRent, OwnerMaintenance, TenantHome,
    │   │                              # TenantRentHistory, TenantMaintenance, NotFound
    │   └── App.jsx                   # Route map
    └── package.json
```

## Setup & Run

### Prerequisites

```
Java 17, Maven, Node.js (18+), npm
```

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

The app starts on `http://localhost:8080` using an in-memory H2 database. Flyway runs the schema migration on startup, then a demo data seeder populates it automatically. No manual database setup needed, and data resets on every restart.

**Demo login (seeded automatically on every dev/H2 startup):**

| Role | Email | Password |
|---|---|---|
| Owner | `alice@owner.com` | `password123` |
| Tenant | `bob@tenant.com` | `password123` |

The seed includes two properties (one occupied, one vacant), a tenant with a lease expiring in 20 days (inside the 30-day reminder window), one paid rent payment, one already-overdue pending payment, one future pending payment, and one open maintenance request.

**Useful local URLs:**
- Swagger UI: `http://localhost:8080/swagger-ui.html` (use the Authorize button with a token from `/api/auth/login`)
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:rentaldb`, user `sa`, no password)

**Switching to MySQL:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Configure via env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`. See `application-mysql.properties`. Seeding is skipped automatically on this profile.

**JWT secret:** set your own before deploying anywhere real:

```bash
export JWT_SECRET="a-long-random-string-at-least-32-characters"
```

### 2. Frontend

Make sure the backend is running first, then in a new terminal:

```bash
cd frontend
npm install
npm run dev
```

Opens on `http://localhost:5173`. If your backend runs somewhere other than `http://localhost:8080`, edit `.env`:

```
VITE_API_BASE_URL=http://localhost:8080/api
```

Sign in with the seeded demo accounts above, or register a new account from `/register` and pick Landlord or Tenant.

## API Reference

### Auth
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | public, body includes `role: OWNER \| TENANT` |
| POST | `/api/auth/login` | public, returns JWT |

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

## Design Notes

- **Owner and Tenant are separate entities and tables**, both authenticating through the same `/api/auth/**` endpoints. `CustomUserDetailsService` checks the Owner table first, then Tenant, by email. `AppUserPrincipal` is the unified Spring Security principal wrapping either one.
- **Property to Tenant is modeled one-to-many**, not one-to-one, so multi-unit properties or tenant history can be added later without a schema change. Today's "one active tenant per property" rule is enforced in `PropertyService.assignTenant`, not in the schema.
- **Maintenance request ownership is checked server side**, in `MaintenanceRequestService.createRequest`. A tenant can only raise a request for the property they're actually assigned to, regardless of what property ID the client sends.
- **RentPayment status transitions are one directional for the scheduler.** `OverdueRentScheduler` only ever moves `PENDING` to `OVERDUE`, never touching `PAID` payments, so a late-but-settled payment is never re-flagged. `markPaid` is the only path that sets `PAID`.
- **The lease expiry job currently only logs.** `LeaseExpiryScheduler` is the extension point for wiring in real notifications (email, SMS, in-app).
- **Schema is owned by Flyway, not Hibernate.** `ddl-auto=validate` means Hibernate only checks entity mappings against the migrated schema; it never creates or alters tables. Add new migrations as `V2__*.sql`, `V3__*.sql`, and so on, and never edit `V1__init_schema.sql` once it has run anywhere.
- **No endpoint currently lists "all tenants for an owner."** Assign Tenant and Create Rent Payment on the frontend both take a raw tenant ID typed in by the owner, matching what the backend accepts today. Worth adding a proper lookup endpoint if this becomes annoying in practice.
- **A 404 from `/tenants/{id}/property` means "not yet assigned,"** not an error. Both `TenantHome` and `TenantMaintenance` treat it as an empty state rather than an error banner.

## Running the Tests

```bash
cd backend
mvn test
```

Covers register/login/duplicate-email/bad-password flows, owner-only property CRUD with cross-owner access denial, tenant-can't-raise-request-for-unassigned-property, the full maintenance status lifecycle including the already-resolved guard, cross-tenant and cross-owner rent payment access denial, and the overdue scheduler's flag/exclude logic (invoked directly rather than waiting for its cron trigger). Tests run against a separate H2 instance and each test method rolls back its own transaction, so they never leak data between runs.

## Suggested Next Steps

1. Run `mvn spring-boot:run`, then open `http://localhost:8080/swagger-ui.html` and try the seeded demo accounts end to end.
2. Run `mvn test` to confirm everything passes in your environment.
3. Wire `LeaseExpiryScheduler` to a real notification channel.
4. Add a tenant lookup endpoint and swap the manual tenant ID fields on the frontend for a real picker.
5. Add toast notifications and pagination as the app grows.
6. Add end to end tests (Playwright or Cypress) covering the owner and tenant happy paths against a running backend.

---

Built by Ashutosh.
