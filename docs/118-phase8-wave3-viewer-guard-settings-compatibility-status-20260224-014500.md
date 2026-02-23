# Phase 8 Wave 3 - Viewer Guard Settings Compatibility Status Metadata (2026-02-23)

## Scope
UI-only usability/safety enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added a guard-settings JSON compatibility status row in the guard-share panel:
  - `Guard settings compatibility: <status> type=<...> version=<...> ...`
- Introduced a shared renderer for guard compatibility metadata across:
  - local guard JSON export
  - button-based guard JSON import
  - drag/drop guard JSON import
  - clear/reset states
  - import/export parse/validation errors
- Supported statuses:
  - `SUPPORTED` (versioned guard settings artifact v1, and local export marker)
  - `LEGACY_MAP` (plain `{ profile, threshold, enabled }` fallback)
  - `ERROR` (parse/validation failures)
  - `UNKNOWN` (cleared/idle state)
- Fixed a UI bug discovered during testing:
  - button click import path passed a `PointerEvent` into the compatibility source label and rendered `source=[object PointerEvent]`
  - parser now normalizes non-string sources to `textarea:guard-settings`
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - export guard JSON -> compatibility row shows `SUPPORTED`, `type=EXPORT_LOCAL`, `version=1`, `source=viewer-export`
  - legacy guard JSON import (button) -> compatibility row shows `LEGACY_MAP`, `source=textarea:guard-settings`
  - versioned guard JSON import (button) -> compatibility row shows `SUPPORTED`, `type=phase8-wave3-import-diff-preset-guard-settings`, `version=1`
  - invalid guard JSON import -> compatibility row shows `ERROR`
  - drag/drop text import -> compatibility row shows `LEGACY_MAP`, `source=drop:guard-settings:text`
  - clear guard JSON -> compatibility row resets to `UNKNOWN`
  - button import source label regression fixed (no more `[object PointerEvent]`)
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 118` evidence path for `pu-precision-audit`
  - embedded snapshot metadata updated (`fp=35698c9ea72a`)
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-194719.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive metadata.
- Rollback: revert this commit; no runtime/data impact.
