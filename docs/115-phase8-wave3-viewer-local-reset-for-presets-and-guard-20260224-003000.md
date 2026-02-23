# Phase 8 Wave 3 - Viewer Local Reset for Presets and Guard Settings (2026-02-23)

## Scope
UI-only usability/safety enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added a one-click reset action for viewer-local triage preset state and overwrite-guard settings.
- New button: `Reset Local Presets + Guard` in the triage preset / guard panel.
- Reset behavior (after confirmation):
  - clears browser-local triage preset storage
  - clears browser-local overwrite-guard settings storage
  - resets in-memory preset list to empty (`Count=0`)
  - resets overwrite guard to default profile/threshold (`default`, `2`)
  - resets preset JSON import panel state (preview `NONE`, compatibility `UNKNOWN`)
- Cancel path preserves all local state and reports cancellation in reset status line.
- Reset applies only to this viewer’s browser-local state (no GS runtime/config impact).
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - seeded local triage preset + custom guard state (`custom`, threshold `4`)
  - reset with `confirm=false` -> state preserved, localStorage entries remain
  - reset with `confirm=true` -> presets cleared, guard reset to `default/2`, both localStorage entries removed, preview/compat rows reset
  - reset prompt contains explicit scope text (`Reset viewer local presets and overwrite-guard settings`)
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 115` evidence path for `pu-precision-audit`
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - result fingerprint updated (`fp=349a5cf7c51c`)
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-192959.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive utility.
- Rollback: revert this commit; no runtime/data impact.
