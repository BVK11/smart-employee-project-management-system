# Postman verification checklist

Base URL: `http://localhost:8080/api`

## Authentication

1. `POST http://localhost:8080/register`

```json
{"name":"Admin User","email":"admin@example.com","password":"Password@123","role":"ADMIN"}
```

2. `POST http://localhost:8080/login`

```json
{"email":"admin@example.com","password":"Password@123"}
```

Save `token` from the response as the Postman collection variable `jwt`.

For protected routes send: `Authorization: Bearer {{jwt}}`.

## Dashboard

| Method | URL | Expected role / status |
|---|---|---|
| GET | `/dashboard/admin` | ADMIN, 200 |
| GET | `/dashboard/employee/1` | ADMIN or EMPLOYEE, 200 |

## Search and filter APIs

| Method | URL |
|---|---|
| GET | `/employees/search?department=Engineering&status=ACTIVE&keyword=john` |
| GET | `/projects/search?priority=HIGH&status=ACTIVE&keyword=portal` |
| GET | `/tasks/search?status=PENDING&deadline=2026-08-01` |

## Reports and exports

| Method | URL | Expected result |
|---|---|---|
| GET | `/reports/employee` | Employee task report JSON |
| GET | `/reports/projects` | Project progress report JSON |
| GET | `/reports/tasks/pending` | Pending task report JSON |
| GET | `/reports/pdf` | 200, `application/pdf`, attachment `report.pdf` |
| GET | `/reports/excel` | 200, Excel media type, attachment `report.xlsx` |

All report routes require ADMIN.

## Security matrix

| Route type | No token | EMPLOYEE token | ADMIN token |
|---|---:|---:|---:|
| `/login`, `/register` | 200/201 | 200/201 | 200/201 |
| Read employee/project/task routes | 401 | 200 | 200 |
| Create/update/delete employee/project/task | 401 | 403 | 200/201/204 |
| `/dashboard/admin`, `/reports/**` | 401 | 403 | 200 |

Also verify invalid request data returns `400` with an `ErrorDetailsDTO`, duplicate employee email returns `409`, missing IDs return `404`, and malformed/expired JWTs return `401`.
