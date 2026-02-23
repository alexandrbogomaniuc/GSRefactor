# Phase 8 Wave 3 Viewer Imported Compare-Report Diff Mode (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Extend the viewer import inspector with side-by-side diff mode for two imported compact compare-report JSON artifacts.
- No GS runtime/protocol behavior change.

## What Changed
### 1) Imported artifact diff mode (A vs B) for compact compare-report JSON
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

Added/extended viewer capabilities:
- Imported A compact compare-report inspector (existing)
- Imported B compact compare-report slot (paste/upload/load-current-preview/clear)
- Imported artifact diff card (`A vs B`) with:
  - summary deltas (overall/policy/fail/pass counts/metric counts)
  - rule status/detail changes list (A vs B)
  - metric delta diff table (deltaMismatch and deltaSnapshots differences)
  - imported B threshold preview

Notes:
- Uses the same compact compare-report JSON schema (`type=phase8-wave3-discrepancy-compare-report`) produced by viewer and CLI compare-export tools.
- Existing raw discrepancy export workflow and compare/export features remain unchanged.

### 2) Operator docs / evidence routing
Updated:
- `support/modernizationRunbook.jsp` (viewer note mentions imported-artifact diff mode)
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer moved to this doc (`doc 104`)

## Test Evidence
- Browser `file://` viewer test ✅
  - sample A + sample B + compare mode + demo policy -> generate compact compare JSON
  - import generated compact compare JSON into Imported A (`Load Current Compare Preview`)
  - switch to strict policy -> generate compact compare JSON -> import into Imported B (`Load Current Compare Preview To B`)
  - imported artifact diff card renders (`A vs B`) with changed rule rows and metric diff table
  - additional manual edit in Imported B JSON (metric deltaMismatch) re-parsed successfully and diff updates
- Dashboard embedded sync ✅
  - checklist evidence path points to `docs/104-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-170410.md`

## Compatibility / Rollback
- Viewer diff mode is additive client-side tooling only; no GS runtime behavior or protocol behavior changed.
- Rollback is isolated to support page UI logic and documentation/checklist references.

## Next Step
- Add drag/drop import UX for compact compare-report JSON and/or filter toggles in the imported artifact diff table (changed-only, rules-only).
