# Phase 8 Wave 3 - Viewer Guard Settings JSON Drag/Drop Import (2026-02-23)

## Scope
UI-only usability enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added drag/drop import support for overwrite-guard settings JSON onto the guard JSON textarea.
- Reused existing drag/drop UI behavior (`drop-target-active`) for consistent visual feedback.
- Drop handler supports:
  - JSON file drop (`dataTransfer.files[0]`)
  - raw JSON text drop (`application/json` or `text/plain`)
- Dropped content is written into the guard JSON textarea and parsed through the existing guard JSON import path.
- Existing import behavior is preserved:
  - versioned guard artifact (`phase8-wave3-import-diff-preset-guard-settings`, `v1`)
  - legacy plain guard map fallback
  - same validation/error status line text as button import path
- Added viewer hint text for guard JSON drag/drop.
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - synthetic drag/drop text path -> legacy plain guard JSON imports and applies (`custom`, threshold `7`)
  - synthetic drag/drop file path -> versioned guard artifact imports and applies (`custom`, threshold `3`)
  - invalid dropped JSON path -> error shown through shared import parser status line
  - drop target wiring present for guard JSON textarea
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 117` evidence path for `pu-precision-audit`
  - embedded snapshot metadata updated (`fp=e86d476abacf`)
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-194141.md`
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
