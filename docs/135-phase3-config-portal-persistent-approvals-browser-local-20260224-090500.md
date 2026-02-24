# Phase 3 Config Portal: Persistent Approvals (Browser Local)

Date: 2026-02-24
Status: Implemented and UI-smoke-tested (refactor GS runtime)

## Goal
- Add a user-friendly persistent approval queue and audit trail to the GS configuration portal without changing legacy GS runtime behavior or backend contracts.
- Keep storage local to the operator browser (`localStorage`) for zero-risk rollout in the current phase.

## Implemented
- Added `Approval Queue` action column to `Session Draft Registry`.
- Added `Level 4b: Persistent Approval Queue (Browser Local)` section with:
  - overview counters,
  - progress bar,
  - queued approvals table,
  - local decision history table,
  - JSON export/import/reset tooling.
- Added browser-local workflow actions:
  - `Queue`, `Approve`, `Publish`, `Reject`, `Rollback`, `Remove`.
- Added bundle format for sharing/restoring local approval state:
  - `type=config-portal-approval-bundle`
  - `version=1`

## Backward Compatibility
- No server-side workflow contract changes.
- No DB/Cassandra changes.
- No legacy portal endpoint/path changes (`/support/configPortal.jsp` unchanged).
- Feature is additive UI logic on top of the existing session draft registry.

## Runtime UI Smoke Test (refactor GS)
- URL: `http://127.0.0.1:18081/support/configPortal.jsp`
- Draft creation verified via portal request (`workflowAction=draft`).
- Browser smoke (executed against running refactor GS):
  - queue draft from session registry,
  - approve queued draft,
  - export bundle JSON,
  - reset local queue/history (confirm),
  - import bundle JSON restore.

## Evidence
- Source UI implementation:
  - `gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- Runtime portal endpoint (HTTP 200):
  - `/support/configPortal.jsp`
- Browser verification performed against refactor runtime (`refactor-gs-1` mapped to host `18081`)

## Next Follow-up (separate increment)
- `ux-rollback-guardrails` completion with visual canary/rollback gating integrated into the same portal workflow area.
