# Phase 8 Wave 3 - Imported Artifact Diff Triage Preset JSON Share (2026-02-23)

## Scope
UI-only enhancement for `/support/phase8DiscrepancyViewer.html` in `file://` mode and deployed GS support page.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added triage preset JSON bundle export/import for imported compact compare-report artifact diff mode.
- Preset bundle supports cross-machine sharing while keeping browser-local (`localStorage`) presets as the default workflow.
- Import behavior merges presets by name and overwrites on name conflict.
- Added file upload import and pasted JSON import textarea for preset bundles.
- Added export-to-textarea + download flow for preset bundles.
- Updated Phase 8 evidence pointers in checklist/dashboard/docs/runbook.

## JSON Bundle Format (Viewer)
- `type`: `phase8-wave3-import-diff-triage-preset-bundle`
- `version`: `1`
- `generatedAtUtc`
- `viewer`
- `presetCount`
- `presets`: map keyed by preset name

Each preset contains:
- `rulesChangedOnly`
- `metricsChangedOnly`
- `ruleStatusPass`
- `ruleStatusFail`
- `ruleStatusInfo`
- `ruleStatusMissing`
- `metricSearch`
- `savedAtUtc`

## Validation Performed
- Browser `file://` smoke (`/support/phase8DiscrepancyViewer.html`)
  - saved local triage preset
  - exported preset bundle JSON (textarea + download path)
  - deleted preset
  - imported bundle JSON back from textarea
  - re-applied preset and verified filters/search restored
  - result sample: exported bundle type `phase8-wave3-import-diff-triage-preset-bundle`, imported preset restored with `metricSearch=template`
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded data refresh shows `doc 108` evidence path for `pu-precision-audit`
  - progress count remains `26/41` (evidence update only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - result: `embedded-checklist synced: 26/41` with new fingerprint
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-172952.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible: viewer-only additive changes.
- Rollback: revert this commit; no data/schema/runtime migration required.
