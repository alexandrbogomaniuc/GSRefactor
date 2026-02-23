# Phase 8 Wave 3 - Imported Artifact Diff Triage Preset Bundle Drag/Drop (2026-02-23)

## Scope
UI-only improvement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added drag/drop import support for triage preset bundle JSON onto the preset JSON textarea (`importDiffPresetJsonInput`).
- Supports dropped file or dropped JSON text (`application/json` / `text/plain`).
- Reuses the existing preset-bundle import validation + merge path (`mergeImportDiffPresetBundleFromText`) to keep behavior consistent with textarea/file button imports.
- Added user-facing viewer hint describing preset bundle drag/drop support.
- Updated checklist evidence/docs/runbook and synced embedded dashboard data (`file://` mode progress page).

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - built imported A/B diff state
  - saved triage preset and exported preset bundle JSON
  - cleared preset store
  - synthetic drop-path test via `handleImportDiffPresetDrop(...)` with dropped JSON text
  - imported preset restored and applied successfully
  - evidence result example: `drop:preset-bundle:text` with restored `metricSearch=templateMaxBet`
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 109` evidence path for `pu-precision-audit`
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - result fingerprint updated (`fp=b9fbe6003d9f`)
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-180440.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive UX change.
- Rollback: revert this commit; no runtime migration/data impact.
