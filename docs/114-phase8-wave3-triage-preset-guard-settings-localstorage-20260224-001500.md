# Phase 8 Wave 3 - Triage Preset Guard Settings LocalStorage Persistence (2026-02-23)

## Scope
UI-only usability enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added browser-local persistence for triage preset overwrite-guard settings using `localStorage`.
- Persisted state includes:
  - guard profile (`strict`, `default`, `relaxed`, `disabled`, `custom`)
  - threshold
  - enabled flag
- Guard settings now restore on page load with fallback to `default` if no saved state exists or stored data is invalid.
- Restored state is reflected in the UI meta line (`restored from browser storage`).
- Existing guard enforcement logic remains unchanged and still applies to textarea/file/drag-drop preset imports via the shared merge path.
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - no saved state -> default fallback (`profile=default`, threshold `2`)
  - saved `disabled` profile -> localStorage contains disabled state and restore reapplies disabled mode (threshold field disabled)
  - saved `custom` threshold `4` -> restore reapplies `Custom` profile with threshold `4`
  - restore meta line includes `restored from browser storage`
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 114` evidence path for `pu-precision-audit`
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - result fingerprint updated (`fp=91d26e2adae4`)
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-192457.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive persistence change.
- Rollback: revert this commit; no runtime/data impact.
