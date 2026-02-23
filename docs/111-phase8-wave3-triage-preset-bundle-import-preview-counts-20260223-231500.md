# Phase 8 Wave 3 - Triage Preset Bundle Import Preview Counts (2026-02-23)

## Scope
UI-only enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added preset-bundle import preview counts before merge in the triage preset import panel.
- New preview action (`Preview Presets JSON`) computes and displays:
  - `incoming`
  - `new`
  - `overwrite`
  - `current`
  - `postMerge`
- Preview row also shows sample affected preset names (`new=` / `overwrite=` excerpts) and source label.
- Import flow now shows a `MERGE_PLAN` preview row before applying merge so operators see the same counts during import execution.
- Error preview resets to `NONE` while compatibility row shows `ERROR` on invalid JSON.
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - built preset bundle with 1 overwrite + 1 new preset
  - preview shows `incoming=2`, `new=1`, `overwrite=1`, `current=1`, `postMerge=2`
  - import shows `MERGE_PLAN` preview and applies merge (final presets include `ExistingPreset`, `NewPreset`)
  - invalid JSON preview resets preview row to `NONE` and compatibility row to `ERROR`
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 111` evidence path for `pu-precision-audit`
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - result fingerprint updated (`fp=037b9d5ef341`)
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-190407.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive UX/visibility change.
- Rollback: revert this commit; no runtime/data impact.
