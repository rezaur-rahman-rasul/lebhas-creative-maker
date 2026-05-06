# Creative SaaS Frontend

Angular 21 Day 1 frontend foundation for the AI-powered creative SaaS platform.

## Stack

- Angular 21 standalone application
- Signals and computed signals
- Functional guards and interceptors
- Tailwind CSS 4
- Lucide Angular icons

## Structure

```text
src/app/
  core/
    api/
    auth/
    guards/
    icons/
    interceptors/
    layout/
    services/
    state/
  features/
    auth/
    dashboard/
    master/
    admin/
    crew/
  shared/
    components/
    directives/
    pipes/
    models/
    utils/
```

## Local Development

```powershell
npm.cmd install
npm.cmd start
```

The local app runs on `http://localhost:4200` and points to the backend gateway at `http://localhost:8080`.

## Builds

```powershell
npm.cmd run build:local
npm.cmd run build:dev
npm.cmd run build:staging
npm.cmd run build
```

## Docker

```powershell
docker compose -f .\docker\docker-compose.yml up --build
```

## Backend Integration

The API client expects the backend response envelope:

```json
{
  "success": true,
  "message": "",
  "data": {},
  "errors": [],
  "timestamp": ""
}
```

HTTP interceptors prepare:

- `Authorization: Bearer <token>`
- `X-Workspace-ID`
- `X-Correlation-ID`
- request loading state
- normalized error notifications
