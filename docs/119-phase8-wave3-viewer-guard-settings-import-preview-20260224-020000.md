# Phase 8 Wave 3 - Viewer Guard Settings Import Preview (2026-02-23)

## Scope
UI-only usability/safety enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added a `Preview Guard JSON` action in the guard-share panel.
- Added a guard import preview status row showing the normalized candidate settings before apply:
  - `profile`
  - `threshold`
  - `enabled`
  - preview mode (for example `threshold_override`, `profile`, `unknown_profile_fallback`)
  - source/type/version metadata
- Added non-mutating guard JSON parsing/inspection helpers used by preview and import paths:
  - shared type/version validation for guard-settings artifacts
  - shared legacy plain-map fallback support
  - shared normalization logic for previewing the post-import effective guard result
- Import path now renders the same preview row before apply, so preview and import show consistent normalized results.
- Fixed a preview error-path source-label bug discovered during testing:
  - button preview click passed a `PointerEvent` and rendered `source=[object PointerEvent]`
  - preview error path now normalizes to `textarea:guard-settings`
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - preview legacy JSON `{ profile: default, threshold: 6 }` -> preview shows normalized `custom / threshold=6 / enabled=true` (`threshold_override`)
  - preview does not apply changes yet (current guard remains `default / 2`)
  - import same JSON -> current guard updates to `custom / 6`
  - invalid preview JSON -> preview and compatibility rows both show `ERROR`
  - clear guard JSON -> preview resets to `NONE`, compatibility resets to `UNKNOWN`
  - preview error source-label regression fixed (`source=textarea:guard-settings`, no `[object PointerEvent]`)
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 119` evidence path for `pu-precision-audit`
  - embedded snapshot metadata updated (`fp=a2fcd2c8d280`)
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-195604.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive metadata/preview.
- Rollback: revert this commit; no runtime/data impact.
