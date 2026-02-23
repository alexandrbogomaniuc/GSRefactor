# Phase 8 Wave 3 Discrepancy Viewer Support Page (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Add a lightweight support/admin presentation page for exported discrepancy JSON (non-prod validation visibility).
- No GS runtime behavior change; compare mode remains opt-in.

## What Changed
### 1) New support viewer page
Added:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

Capabilities:
- read-only view of JSON produced by `phase8-precision-wave3-discrepancy-export.sh`
- file upload (`.json`) for exported reports
- pasted JSON parsing
- embedded sample JSON for immediate demo/testing
- summary cards (`parser`, `generatedAtUtc`, snapshot/metric counts, mismatch metrics)
- metrics table with mismatch highlighting and mismatch-only filter
- raw JSON preview
- works in `file://` mode (no server fetch required)

### 2) Operator docs/runbook visibility
Updated:
- `support/modernizationDocs.jsp` operator pages + Phase 8 doc list
- `support/modernizationRunbook.jsp` Wave 3 export section note pointing to the viewer
- checklist evidence pointer updated to this doc (`doc 96`)

## Test Evidence
- Browser test (`file://`) ✅
  - page loads and auto-renders embedded sample
  - mismatch metric row highlighted (`templateMaxBetScale2`)
  - summary shows snapshot/metric counts
- Dashboard embedded sync ✅
  - checklist evidence path now points to `docs/96-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-162110.md`
  - summary: `PASS=38, FAIL=0, SKIP=0`

## Compatibility / Rollback
- Viewer is a standalone support page and does not affect legacy or refactor runtime behavior.
- Rollback is isolated to the new HTML page + docs/checklist references.

## Next Step
- Continue Phase 8 Wave 3 by adding a small operator workflow example (sample export command -> viewer import path -> validation checklist) and/or optional JSON diff view between multiple export runs.
