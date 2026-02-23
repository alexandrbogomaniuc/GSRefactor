# Phase 8 Wave 3 Viewer Compact Comparison Report Export (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Add compact comparison report export from the discrepancy viewer (JSON/Markdown, client-side only).
- No GS runtime behavior change.

## What Changed
### 1) Compact comparison report export in viewer
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

New capabilities:
- compact comparison report generation (client-side) from current A/B compare state and threshold validation state
- JSON report preview (includes thresholds, summary, rules, metric deltas)
- Markdown report preview (operator-friendly summary + table)
- download buttons for JSON and Markdown report files
- export payload uses the same guided-validation rule evaluation as on-screen PASS/FAIL badges

### 2) Operator docs visibility
Updated:
- `support/modernizationRunbook.jsp` viewer note (mentions compact comparison report export)
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer moved to this doc (`doc 99`)

## Test Evidence
- Browser `file://` viewer test ✅
  - sample A + sample B + compare mode + demo thresholds produce PASS validation
  - compact comparison JSON report preview populated
  - compact comparison Markdown report preview populated
  - export summary meta shows `overall=PASS` and metric count
- Dashboard embedded sync ✅
  - checklist evidence path now points to `docs/99-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-163721.md`
  - summary: `PASS=38, FAIL=0, SKIP=0`

## Compatibility / Rollback
- Viewer remains a standalone support page; no GS/runtime/protocol behavior changes.
- Rollback is isolated to viewer UI logic and docs/checklist references.

## Next Step
- Add reusable threshold policy profiles (named presets) and/or parser-side compact comparison CLI export for non-UI automation.
