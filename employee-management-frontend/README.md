# Smart EMS — Frontend

Enterprise Management System frontend built with React, TypeScript, Vite, and Tailwind CSS. Integrates with a Spring Boot backend running at `http://localhost:8080`.

## Stack

- React 18 + TypeScript + Vite
- Tailwind CSS
- React Router v6
- Axios (with JWT request/response interceptors)
- React Hook Form + Zod validation
- Recharts (dashboard charts)
- Lucide React icons

## Design

The UI follows the "Structured Ledger" design system: IBM Plex Sans/Mono typography, a dark 64px icon rail, an amber accent, hairline dividers, and mono-numeric ledger tables. Fully responsive down to mobile (rail collapses into a slide-out drawer, KPI grids and tables reflow).

## Getting Started

```bash
npm install
cp .env.example .env
npm run dev
```

The app runs at `http://localhost:5173` and expects the backend at the URL in `.env` (`VITE_API_BASE_URL`, default `http://localhost:8080`).

```bash
npm run build     # production build
npm run preview   # preview the production build
```

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `VITE_API_BASE_URL` | Base URL of the Spring Boot backend (no trailing slash, no `/api`) | `http://localhost:8080` |

## Authentication & Roles — Important Note

The backend's `/login` endpoint returns only `{ token }`. It does **not** return the user's role, employee ID, name, or expose a `/me` endpoint.

To work around this, the app includes a **temporary, clearly-isolated** frontend mechanism:

- After login, if no role is stored locally, the user is sent to `/select-role` to choose **ADMIN** or **EMPLOYEE** (and, for employees, their Employee ID). This choice is stored in `localStorage` only, purely to drive which UI/navigation is shown.
- This is **not** a security boundary. Real authorization is enforced by the backend via JWT + Spring Security on every request. If the backend rejects a request with `403`, the UI will surface that error — the local role flag only changes what's *offered* in the UI, not what's actually permitted.
- All of this logic lives in `src/utils/session.ts` and `src/context/AuthContext.tsx`, clearly commented, so it can be deleted and replaced once the backend adds a real profile/`me` endpoint.

The Employee Dashboard similarly requires an Employee ID (via selector for admins, or a manual input for employees) since the backend has no way to resolve "current employee" from the JWT alone.

## Project Structure

```
src/
  api/            Axios client + one module per resource (auth, employees, projects, tasks, dashboard, reports)
  components/
    layout/       Sidebar, Topbar, AppLayout
    ui/           Reusable Badge, Modal, ConfirmDialog, Pagination, FormField, Skeleton, EmptyState, KpiCard
  context/        AuthContext, ToastContext
  pages/          Auth, Dashboard, Employees, Projects, Tasks, Reports
  routes/         ProtectedRoute, AdminRoute
  types/          TypeScript interfaces matching every API request/response
  utils/          session (localStorage helpers), format (currency/date)
```

## Error Handling

All API errors follow the backend's uniform shape:

```json
{ "timestamp": "...", "message": "...", "details": { "field": "message" }, "httpStatus": 400 }
```

- Field-level errors (`details`) are mapped onto the relevant form field.
- `message` is shown as a toast / inline banner.
- `401` clears the session and redirects to `/login`.
- `403` surfaces a friendly permission-denied message.
- `404` / `409` / `500` get friendly fallback copy if the backend doesn't supply a message.

## Reports

The Reports page downloads PDF/Excel exports as blobs, respecting the `Content-Disposition` filename header when present (falls back to `report.pdf` / `report.xlsx`).

## Notes

- Task's employee selector is filtered to employees already assigned to the selected project, per the business rule that an employee must be on a project before receiving a task on it.
- Setting task progress to 100 or status to COMPLETED lets the backend apply its own auto-sync rule (progress↔status); the UI simply reflects whatever the backend returns after each update.
- No mock backend or fake data is used; all screens read live from the API contracts above. Empty and loading states are handled explicitly.
