# Phase 8 Wave 3 Imported Report Diff Triage Filters (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Improve imported compact compare-report artifact triage in the viewer with rule-status filtering and metric-name search.
- No GS runtime/protocol behavior change.

## What Changed
### 1) Rule-status class filters for imported artifact diff mode
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

Added rule-status filters for imported artifact diff card (`A vs B`):
- `PASS`
- `FAIL`
- `INFO`
- `MISSING`

Behavior:
- filters apply to the rule-change list and can be combined with `Show changed rules only`
- diff metadata now reports visible rule count and active status filter set

### 2) Metric-name search for imported artifact diff mode
Added metric triage search:
- `Metric name search` text input
- `Clear Search` action

Behavior:
- filters imported artifact diff metric rows by metric name substring (case-insensitive)
- works together with `Show changed metrics only`
- metrics footer now reports visible row count and active search value

## Test Evidence
- Browser `file://` viewer test ✅
  - imported artifact diff mode renders for Imported A/B compact compare reports
  - rule-status filter `FAIL only` reduces visible rule rows (`11 -> 5` in test)
  - metric search `templateMaxBet` reduces visible metric rows (`3 -> 1` when changed-only metrics filter disabled)
  - `Clear Search` restores visible metric rows (`1 -> 3`)
- Dashboard embedded sync ✅
  - checklist evidence path points to `docs/106-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-171950.md`

## Compatibility / Rollback
- Viewer triage filters are additive and client-side only; no GS runtime behavior or protocol behavior changed.
- Rollback is isolated to support page UI logic and documentation/checklist references.

## Next Step
- Add saved local triage presets for imported artifact diff mode (for example, FAIL-only + changed-only + search pattern) to speed repeated operator workflows.
