# Phase 8 Wave 3 Viewer Import For CLI Compare Report JSON (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Add viewer import/inspection support for compact compare-report JSON artifacts (same format produced by viewer/CLI compare export tools).
- No GS runtime/protocol behavior change.

## What Changed
### 1) Viewer import/inspection for compact compare-report JSON
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

Added new UI section:
- `Import Compact Comparison Report (Viewer/CLI JSON)`

Capabilities:
- paste compact compare-report JSON (`type=phase8-wave3-discrepancy-compare-report`)
- upload JSON file for import
- load current compare preview JSON directly into the import inspector
- clear imported report
- inspect imported report summary (overall, policy, rule counts, mismatch deltas)
- inspect imported rules and thresholds (including CLI `overridesApplied` metadata)
- inspect imported metric delta rows (including `compareOnlyInB`)

Compatibility:
- supports viewer-generated compact compare JSON and CLI-generated compact compare JSON (same report format)
- keeps existing A/B discrepancy export workflow unchanged

### 2) Operator docs / evidence routing
Updated:
- `support/modernizationRunbook.jsp` (viewer note mentions compact compare-report JSON import)
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer moved to this doc (`doc 103`)

## Test Evidence
- Browser `file://` viewer test ✅
  - sample A + sample B + compare mode + demo policy profile + compact compare JSON generation
  - `Load Current Compare Preview` imports generated compact report JSON into the new inspector
  - imported summary shows `PASS` and policy `demo_sample_pass`
  - imported rules list/threshold JSON/metric delta table render
- Dashboard embedded sync ✅
  - checklist evidence path points to `docs/103-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-165808.md`

## Compatibility / Rollback
- Viewer import is additive client-side tooling only; no GS runtime behavior or protocol behavior changed.
- Rollback is isolated to support page UI logic and documentation/checklist references.

## Next Step
- Add direct viewer import from a CLI compare-report file drop plus optional side-by-side diff between two imported compact compare reports (operator artifact review mode).
