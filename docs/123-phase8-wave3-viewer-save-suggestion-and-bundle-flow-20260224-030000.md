# Phase 8 Wave 3 - Viewer Save Suggestion + Bundle Flow (2026-02-23)

## Scope
UI-only operator workflow enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added one-click `Save Suggestion + Bundle JSON` action to the `Suggestion Summary (Pre-Save)` panel.
- New flow now:
  - saves the currently suggested triage preset into the local preset store (same behavior as `Save Preset`), then
  - exports a combined bundle JSON artifact (`type=phase8-wave3-import-diff-triage-preset-suggestion-bundle`, `version=1`).
- Bundle artifact includes:
  - `savedPreset` (`name`, preset payload, `storagePersisted` flag)
  - nested `suggestionSummary` artifact (`type=phase8-wave3-import-diff-triage-preset-suggestion-summary`, `version=1`)
- Refactored preset save path into reusable helper (`saveImportDiffPresetByName`) and reused it from the existing `Save Preset` path to avoid behavior drift.
- Preserves existing non-auto-save suggestion behavior until this explicit one-click action is invoked.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`) [pre-finalization for this increment]
  - built Imported A/B compact compare reports from embedded samples
  - generated artifact-based triage suggestion
  - clicked `Save Suggestion + Bundle JSON`
  - verified preset count increases by 1 and saved preset is present in `importDiffPresets`
  - intercepted download and verified bundle JSON downloads/parses with expected `type/version`
  - verified nested `suggestionSummary` artifact is present and typed correctly
  - verified summary panel remains populated after export
- Embedded dashboard sync
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - synced checklist remains `26/41` (tooling/evidence increment only)
  - embedded fingerprint updated (`fp=b9d35a57e168`)
- Evidence path propagation checks
  - `modernization-checklist.json`, `modernizationDocs.jsp`, and synced `modernizationProgress.html` all reference `docs/123-phase8-wave3-viewer-save-suggestion-and-bundle-flow-20260224-030000.md`
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-201738.md`
  - summary: `pass=40 fail=0 skip=0`
- Git whitespace check
  - `git -C /Users/alexb/Documents/Dev/Dev_new diff --check` (clean)

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive workflow/export.
- Rollback: revert this commit; no runtime/data impact.
