# Phase 3 Config Portal: Publish/Rollback Guardrails Visualization

Date: 2026-02-24
Status: Implemented and runtime UI-smoke-tested (refactor GS runtime)

## Goal
- Add visible publish/rollback guardrails and canary readiness controls to the GS configuration portal.
- Improve operator safety without changing backend workflow contracts or blocking legacy-compatible behavior.

## Implemented (Additive UI)
- Added `Level 4c: Publish/Rollback Guardrails + Canary Controls (Browser Local)` panel in `/support/configPortal.jsp`.
- Visual rule table with statuses (PASS/WARN/FAIL) for:
  - workflow validation,
  - config sync state,
  - selected bank canary coverage (`SESSION_SERVICE_CANARY_BANKS`),
  - local operator confirmations (canary/rollback plan/peer review/comms).
- Summary pills + readiness text for quick operator scanning.
- Browser-local persisted checks (`localStorage`) scoped by `bankId + draftVersion`.
- `Publish` / `Rollback` button warning guard:
  - if local guard checks are incomplete, show confirm dialog,
  - cancel keeps workflow action unchanged,
  - proceed remains available (no backend enforcement change).

## Backward Compatibility
- No endpoint/path changes (`/support/configPortal.jsp` unchanged).
- No config-service API contract changes.
- No server-side workflow state machine changes.
- Guardrails are UI-only visualization and local operator checks.

## Runtime UI Smoke Test (refactor GS)
- URL: `http://127.0.0.1:18081/support/configPortal.jsp`
- Tested on draft `ux-rg-001`, bank `271`:
  - panel renders with canary bank detection (`SESSION_SERVICE_CANARY_BANKS=271`) -> PASS
  - local checkbox persistence stored at `configPortalWorkflowGuardrails.v1.271.ux-rg-001`
  - publish click with incomplete checks triggers warning confirm
  - cancel path prevents submit and keeps workflow status `DRAFT`

## Evidence
- Source:
  - `gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- Checklist closure:
  - `gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- Runtime page validated in refactor container (`refactor-gs-1`, host `18081`)

## Next Follow-up (separate increment)
- Optional backend-enforced policy mode (feature-flagged) after formal approval, using the same UI panel as operator precheck surface.
