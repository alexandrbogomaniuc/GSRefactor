# Phase 8 Wave 3 - Viewer Artifact-Based Triage Preset Suggestions (2026-02-23)

## Scope
UI-only operator workflow enhancement for `/support/phase8DiscrepancyViewer.html`.
No GS runtime/protocol/financial behavior changes.

## What Changed
- Added `Suggest Triage Preset (A/B)` action in the imported artifact diff triage preset section.
- Suggestion generator uses imported compact compare-report artifacts (`Imported A` / `Imported B`) to produce a focused triage preset and applies it to the current UI filters without auto-saving.
- Generated suggestion sets:
  - triage filter toggles (rules changed only / metrics changed only)
  - rule-status filters (`PASS/FAIL/INFO/MISSING`)
  - metric search (top metric selected from artifact data)
  - preset name (timestamped, sanitized)
- Suggestion rationale/status is shown in a dedicated footnote and includes source, changed rule counts, fail/missing counts, top metric, and metric selection reason.
- Suggestions do **not** write to local preset storage until the operator explicitly clicks `Save Preset`.

## Suggestion Heuristic (Current)
- Prefer `Imported A + Imported B` diff when both are loaded.
- Choose a top metric for focus using artifact metrics/deltas, prioritizing:
  - changed metric rows between imported artifacts
  - metrics only present in B / compare-only indicators
  - larger mismatch and snapshot deltas
- Fall back to single imported artifact (`B`, else `A`) when only one is loaded.
- Default suggestion is a triage-focused filter (`FAIL` + `MISSING`, changed-only when A/B diff is available) and metric search on the selected top metric.

## Validation Performed
- Browser `file://` viewer smoke (`/support/phase8DiscrepancyViewer.html`)
  - loaded embedded sample A/B runtime discrepancy exports
  - generated compact compare report with `strict` profile and imported into `Imported A`
  - generated compact compare report with `demo_sample_pass` profile and imported into `Imported B`
  - clicked `Suggest Triage Preset (A/B)` and verified:
    - current triage filters updated (changed-only enabled, PASS disabled, FAIL enabled)
    - metric search populated from artifact-derived top metric
    - preset name filled with `suggested-*`
    - suggestion status line includes rationale (`source=A+B`, rule counts, `topMetric=...`)
    - preset store count unchanged before/after (not auto-saved)
- Browser `file://` dashboard reload (`/support/modernizationProgress.html`)
  - embedded checklist shows `doc 121` evidence path for `pu-precision-audit`
  - embedded snapshot metadata updated (`fp=ad5b8942080f`)
  - count remains `26/41` (evidence/tooling increment only)
- Embedded dashboard sync script
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
- Local verification suite
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-200516.md`
  - summary: `pass=40 fail=0 skip=0`

## Files Updated
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## Compatibility / Rollback
- Backward compatible, viewer-only additive helper.
- Rollback: revert this commit; no runtime/data impact.
