# GS Configuration Portal - All Levels Spec

## Scope
This document defines the operator-facing configuration portal baseline implemented at:
- `/support/configPortal.jsp`

The portal is read-only for now and is designed as the base for Phase 3 publish workflow.

## Levels
1. Level 1 - Cluster
- Source: `cluster-hosts.properties` (classpath)
- Purpose: centralize non-hardcoded host/port endpoints used by GS and refactor containers.
- Current UI: key/value table with search.

1b. Level 1b - Session Outbox Safety Controls
- Source: `cluster-hosts.properties` (classpath)
- Purpose: expose retry/DLQ/replay controls for canary-safe outbox operations.
- Current UI: key/value + operator description table for:
  - `SESSION_SERVICE_OUTBOX_RELAY_ENABLED`
  - `SESSION_SERVICE_OUTBOX_TOPIC`
  - `SESSION_SERVICE_OUTBOX_DLQ_TOPIC`
  - `SESSION_SERVICE_OUTBOX_MAX_ATTEMPTS`
  - `SESSION_SERVICE_OUTBOX_RETRY_BASE_MS`
  - `SESSION_SERVICE_OUTBOX_BATCH_LIMIT`
  - `SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT`
  - `SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS`

2. Level 2 - Bank Settings Catalog
- Source: `BankInfo` annotated static keys.
- Purpose: show all supported bank-level settings and what each key means.
- Current UI columns: key, type, category, mandatory, description.
- Categories: Wallet, Bonus/FRB, Multiplayer, Integration URL, Game Limits, Legacy MQ, General.

3. Level 3 - Effective Bank Values
- Source: `BankInfoCache` runtime values for selected bank.
- Purpose: show actual current values that GS is using.
- Current UI: bank identity summary + effective property table.

4. Level 4 - Workflow Scaffold
- Source: portal request parameters + runtime validation checks.
- Purpose: provide operator flow shape for `draft -> validate -> approve -> publish -> rollback` before write-path activation.
- Current UI:
  - draft metadata (`draftVersion`, `changeReason`),
  - workflow status (`DRAFT`, `VALIDATED`, `APPROVED`, `PUBLISHED`, `ROLLED_BACK`),
  - validation checks (cluster config presence + selected bank + mandatory key presence),
  - session draft registry (latest 20 versions with status and timestamp).

## Config Service Bridge (feature flag)
- Flag: `CONFIG_PORTAL_USE_CONFIG_SERVICE` in `cluster-hosts.properties`.
- When `true`: workflow actions are forwarded to `config-service` API; UI shows execution/sync status.
- When `false` or service unavailable: portal automatically falls back to local scaffold behavior.

## Current Operator Flow
1. Open `/support/configPortal.jsp`.
2. Select a bank.
3. Use search to filter by key/type/category/description/value.
4. Run workflow buttons (`Save Draft`, `Validate`, `Approve`, `Publish`, `Rollback`) in safe scaffold mode.
5. If direct edit is required, click `Open Bank Editor` (existing compatibility tool).

## Why this is backward-compatible
- No existing endpoint contracts changed.
- Existing bank edit flow remains unchanged.
- New portal is additive and safe (workflow scaffold + session metadata only, no config write path).

## Planned Extension (next increments)
1. Add provenance column (`cluster`, `bank`, `default`) for effective values.
2. Replace scaffold workflow with persistent workflow storage and approvals.
3. Add portal module tabs required by modernization plan:
- clusters,
- banks,
- games,
- currencies,
- promos.
4. Add version and audit metadata (`configVersion`, author, timestamp, change reason).
