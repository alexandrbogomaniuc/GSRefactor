# Phase 8 Wave 3 - Viewer Guard Preview Diff vs Current State (2026-02-23)

## Scope
UI-only usability/safety enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added a `Guard preview vs current` diff row in the guard-share panel.
- The diff row compares the preview candidate settings against the currently applied guard state and shows:
  - `DIFF` with changed fields (for example `profile,threshold`)
  - `SAME` when candidate equals current
  - `ERROR` on preview parse/validation failure
  - `NONE` when no preview is active
- Preview/import/export/clear/error flows now update the diff row consistently.
- Import path re-renders the preview row after apply so the diff row flips from `DIFF` (pre-apply) to `SAME` (post-apply) when the candidate is successfully applied.
- Updated checklist/docs/runbook references and synced embedded dashboard data.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - current guard `default/2`, preview legacy JSON `{profile: default, threshold: 6}` -> diff row shows `DIFF` with `changed=profile,threshold`
  - import same JSON -> diff row updates to `SAME` and current guard becomes `custom/6`
  - invalid preview JSON -> preview/compat/diff rows all show `ERROR`
  - clear guard JSON -> preview row `NONE`, diff row `NONE`, compat row `UNKNOWN`
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 120` evidence path for `pu-precision-audit`
  - embedded snapshot metadata updated (`fp=bb61a9992f54`)
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-200015.md`
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
