# Phase 8 Wave 3 - Triage Preset Guard Profiles and Configurable Threshold (2026-02-23)

## Scope
UI-only safety/usability enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added configurable overwrite-guard profiles + threshold controls for triage preset bundle imports.
- New guard profiles in the preset import panel:
  - `Strict` (confirm on `overwrite >= 1`)
  - `Default` (confirm on `overwrite >= 2`)
  - `Relaxed` (confirm on `overwrite >= 5`)
  - `Disabled` (no overwrite confirmation prompt)
  - `Custom` (operator-defined threshold)
- Added threshold input + profile apply button; threshold edits switch profile to `Custom`.
- Guard confirmation prompt now includes active profile name and threshold.
- Guard status/meta rows now reflect the active profile and whether guard is enabled.
- Existing shared merge path remains the single enforcement point (textarea/file/drag-drop imports all inherit the configured guard behavior).
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - `Default` profile (`>=2`) with `overwrite=1` -> no prompt, import proceeds
  - `Strict` profile (`>=1`) with `overwrite=1` -> prompt shown, cancel path preserves presets and shows `MERGE_CANCELLED`
  - `Disabled` profile -> no prompt, import proceeds; threshold input disabled
  - `Custom` threshold `3` with `overwrite=1` -> no prompt, import proceeds; profile switches to `Custom`
  - confirm prompt includes `Profile: strict` and threshold text
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 113` evidence path for `pu-precision-audit`
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - result fingerprint updated (`fp=64e9836e77b7`)
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-191719.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive UX/safety enhancement.
- Rollback: revert this commit; no runtime/data impact.
