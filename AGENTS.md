# AGENTS.md

## Project Scope

This repository is an AI operations troubleshooting assistant.

- Frontend: Vue 3 + TypeScript + Vite + Ant Design Vue.
- Backend: Spring Boot 3, Java 17, MyBatis-Plus, Spring Security, JWT.
- Storage: MySQL 8, Redis, Flyway migrations.
- Deployment: Docker Compose.

These instructions apply to the whole repository. Backend-specific work should follow the rules below.

## Backend Development Rules

- Keep the backend as a modular monolith. Add code under the existing package root `com.example.devopsai`.
- Organize backend code by business module, following the existing style:
  - `auth`: login, current user, JWT, user principal.
  - `user`: user entity and persistence.
  - `diagnosis`: troubleshooting sessions, messages, results, history.
  - `ai`: model configuration, AI client, diagnosis orchestration, AI call logs.
  - `prompt`: prompt templates.
  - `risk`: risk command detection and risk levels.
  - `model`: model provider/config management.
  - `system`: health and system endpoints.
  - `common`: shared response objects, pagination, exceptions, error handling.
  - `config`: Spring, security, MyBatis-Plus, web, and logging configuration.
- Prefer extending existing services, mappers, entities, and response wrappers over introducing parallel patterns.
- Keep controllers thin. Put business logic in services and persistence logic in MyBatis-Plus mappers.
- Return API responses through the existing `ApiResponse` / `PageResponse` conventions.
- Use `BusinessException` for expected business failures and let `GlobalExceptionHandler` translate exceptions.
- Validate external request payloads with Jakarta Bean Validation annotations where appropriate.
- Do not expose secrets, API keys, JWT signing keys, database passwords, or raw provider credentials in logs or responses.
- Do not commit `.env`, generated logs, build outputs, or local dependency artifacts.

## Database Rules

- Manage schema and seed changes with Flyway migrations under `backend/src/main/resources/db/migration`.
- Never edit an already-applied migration in normal development. Add a new versioned migration instead.
- Keep entity fields, database columns, mapper behavior, and API DTOs consistent.
- Use MyBatis-Plus pagination and query wrappers where they match existing code.

## Security Rules

- Preserve the Spring Security + JWT authentication flow.
- New non-login APIs should require authentication unless the feature explicitly needs to be public.
- Respect existing permission naming style such as `diagnose:create` and `model:config`.
- Avoid adding broad CORS, disabled CSRF/security bypasses, or unauthenticated administrative endpoints.
- Log authentication and AI provider failures without leaking tokens or credentials.

## AI Integration Rules

- Keep AI provider calls isolated in the `ai` module. Business modules should not depend directly on a vendor SDK.
- Preserve support for OpenAI-compatible APIs and configurable model settings.
- Record AI call latency, token usage when available, provider errors, and model information through the existing AI call log flow.
- Treat AI output as untrusted input. Parse and validate structured AI responses before storing or returning them.
- For troubleshooting commands returned by AI, run them through risk detection before exposing them to users.

## API Rules

- Keep the base API path under `/api`.
- Match the documented contract in `docs/API_DESIGN.md` unless the task explicitly changes the contract.
- Update documentation when adding, removing, or changing public API behavior.
- Keep error codes and response shapes stable for frontend compatibility.

## Verification

- For backend changes, run from `backend/`:

```bash
mvn test
```

- When changing application startup, configuration, migrations, or security wiring, also run:

```bash
mvn spring-boot:run
```

- If tests or startup cannot be run because local services are unavailable, state exactly what was not verified and why.

## Codex Guidance Location

- Durable Codex development guidance belongs in `AGENTS.md`.
- Put repository-wide guidance in the repository root `AGENTS.md`.
- Put backend-only overrides in `backend/AGENTS.md` if the backend later needs rules that should not apply to the frontend.
- Do not use `.rules` files for coding conventions. Codex `.rules` files are for command approval policy, not project development instructions.
