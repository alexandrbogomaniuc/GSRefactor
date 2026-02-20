# Phase 3 - Config Service Foundation (Microservice Extraction #1)

## Scope
This increment introduces the first standalone refactor microservice:
- `gs-server/refactor-services/config-service`

It is additive and does not change legacy GS request/response contracts.

## Delivered
1. New service implementation
- File-backed draft/version workflow store.
- Workflow actions: `draft`, `validate`, `approve`, `publish`, `rollback`.
- Outbox event queue for Kafka publisher integration.

2. HTTP API (v1)
- `GET /health`
- `GET /api/v1/config/drafts?bankId=...`
- `GET /api/v1/config/drafts/:draftVersion`
- `POST /api/v1/config/drafts`
- `POST /api/v1/config/workflow/{validate|approve|publish|rollback}`
- `GET /api/v1/outbox?status=NEW`
- `POST /api/v1/outbox/:eventId/ack`

API contract file:
- `gs-server/refactor-services/contracts/openapi/config-service-v1.yaml`

3. Refactor stack integration
- Added `config-service` container to refactor compose.
- Added centralized host/port keys:
  - `CONFIG_SERVICE_HOST`
  - `CONFIG_SERVICE_PORT`
  - `CONFIG_PORTAL_USE_CONFIG_SERVICE`

4. Portal bridge (feature-flagged)
- `/support/configPortal.jsp` now attempts remote workflow sync to `config-service` when `CONFIG_PORTAL_USE_CONFIG_SERVICE=true`.
- On errors/unavailable service, portal auto-falls back to local workflow scaffold.
- UI now surfaces:
  - execution mode (`config-service` vs `local-scaffold`),
  - sync status/message,
  - session registry fields for mode/sync/operator.

## Evidence
- Health check:
  - `curl -fsS http://127.0.0.1:18072/health`
- Draft lifecycle checks:
  - create draft -> status `DRAFT`
  - validate draft -> status `VALIDATED`
- Outbox checks:
  - `GET /api/v1/outbox?status=NEW` returns `config.draft.saved` and `config.workflow.validate` events.

## Backward-compatibility statement
- Legacy GS endpoints, MP flow, casino side contracts remain unchanged.
- Existing runtime compose stack is untouched.
- New service runs only in isolated `refactor` stack.

## Risks / gaps
- Storage is currently file-based (`/data/config-workflow-store.json`) and local to container volume.
- No authn/authz yet on config workflow endpoints.
- No Kafka publisher worker yet (outbox only).
- Node dependency scan reported high-severity advisories in transitive packages; remediation planned in dependency-hardening wave.

## Next actions
1. Add persistent storage backend (Cassandra table or dedicated config DB) with version index and approver identity.
2. Implement outbox dispatcher to Kafka with idempotent publish and retry policy.
3. Integrate portal workflow buttons with `config-service` API (feature-flagged, fallback to local scaffold).
4. Add canary publish guard by bank and rollback target version checks.
