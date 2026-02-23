# Phase 8 Wave 3 - Viewer Guard Settings JSON Share (2026-02-23)

## Scope
UI-only usability/safety enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added overwrite-guard settings JSON export/import in the triage preset / guard panel.
- New controls:
  - `Export Guard JSON`
  - `Import Guard JSON`
  - `Clear Guard JSON`
  - guard JSON textarea/status line for cross-machine copy/paste
- Export writes a versioned artifact:
  - `type=phase8-wave3-import-diff-preset-guard-settings`
  - `version=1`
  - `guard: { profile, threshold, enabled }`
- Import supports:
  - current versioned artifact (`type/version`)
  - legacy plain guard map fallback (`{ profile, threshold, enabled }`)
- Import applies settings through the existing guard profile/custom logic and local persistence path.
- Viewer local reset now also clears the guard JSON share textarea/status.
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - set custom guard (`profile=custom`, `threshold=4`) and exported JSON artifact
  - changed state, imported exported JSON, and verified restore to `custom/4`
  - invalid JSON import shows error in guard JSON status line
  - clear action resets guard JSON textarea/status line
  - legacy plain guard map fallback verified:
    - `{ profile: "disabled", threshold: 9, enabled: false }` -> imports as disabled guard
    - `{ profile: "custom", threshold: 5, enabled: true }` -> imports as custom threshold `5`
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 116` evidence path for `pu-precision-audit`
  - count remains `26/41` (evidence/tooling increment only)
  - embedded snapshot metadata visible (`fp=ec6cd9205d3c`)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - result fingerprint updated (`fp=ec6cd9205d3c`)
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-193659.md`
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
