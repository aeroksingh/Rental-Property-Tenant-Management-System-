# Ledger — Rental Management Frontend

React + Vite frontend for the Rental Property & Tenant Management System
backend. Plain JavaScript (no TypeScript), Tailwind CSS, React Router.

## Running it

Make sure the backend is running first (default: `http://localhost:8080`),
then:

```bash
npm install
npm run dev
```

Opens on `http://localhost:5173`. If your backend runs somewhere else, edit
`.env`:

```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Demo login

If you seeded the backend (it does this automatically on a fresh H2 dev
run — see backend README), you can sign in immediately with:

- **Landlord:** `alice@owner.com` / `password123`
- **Tenant:** `bob@tenant.com` / `password123`

Or just register a new account from `/register` — pick "Landlord" or
"Tenant" at the top of the form.

## What's here

```
src/
  api/            One file per backend resource (auth, property, tenant,
                   rent-payments, maintenance-requests). All requests go
                   through axiosClient.js, which attaches the JWT and
                   redirects to /login on 401.
  context/        AuthContext - holds the logged-in user + token in
                   localStorage, exposes login/register/logout.
  components/     Shared UI: Button, TextField, SelectField, Modal, Banner,
                   StatusDot (color-coded status indicator), Sidebar,
                   AppLayout, ProtectedRoute.
  pages/          One page per route - see App.jsx for the full route map.
```

## Design notes

- **Role-gated routing**: `ProtectedRoute` redirects unauthenticated users to
  `/login`, and redirects users hitting a route for the wrong role back to
  their own home page — a tenant can't navigate to `/owner/properties` by
  typing the URL.
- **No endpoint exists to list "all tenants for an owner"** in the current
  backend API, so Assign Tenant and Create Rent Payment both take a raw
  Tenant ID typed in by the owner (matching exactly what the backend
  endpoints accept). This works, but isn't great UX — worth adding a
  `GET /api/properties/{id}/tenant` or including `tenantId` directly in
  `PropertyResponse` on the backend if this becomes annoying in practice.
  Once that exists, swap the manual ID inputs for a proper tenant picker.
- **Lease-expiry warning is computed client-side** in `TenantHome.jsx` from
  `leaseEndDate` — it's a light echo of the backend's `LeaseExpiryScheduler`
  logic (same 30-day window), shown directly to the tenant rather than only
  logged for the owner.
- **404 from `/tenants/{id}/property` means "not yet assigned"**, not a real
  error — both `TenantHome` and `TenantMaintenance` treat it as an empty
  state rather than surfacing an error banner.

## Next steps worth considering

1. Add a **tenant picker** once the backend exposes a lookup endpoint (see
   above), replacing the manual Tenant ID fields.
2. **Toast notifications** instead of inline success/error banners, if the
   app grows more pages.
3. **Pagination** on rent history / maintenance lists once seed data gets
   large.
4. **E2E tests** (Playwright/Cypress) covering the owner and tenant happy
   paths end to end against a real running backend.
