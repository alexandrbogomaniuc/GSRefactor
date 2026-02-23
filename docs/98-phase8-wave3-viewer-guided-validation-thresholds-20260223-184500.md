# Phase 8 Wave 3 Viewer Guided Validation Thresholds (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Extend the discrepancy viewer compare mode with guided validation thresholds and pass/fail badges for non-prod operator validation.
- No GS runtime behavior change.

## What Changed
### 1) Guided validation thresholds in viewer
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

New capabilities:
- operator threshold inputs (client-side only):
  - max mismatch metrics (A)
  - max mismatch metrics (B)
  - max mismatch delta (B-A)
  - max snapshot delta (B-A)
  - allow/disallow new metrics in B
- validation summary cards with overall PASS/FAIL and rule counts
- rule checklist with pass/fail/info badges and explanation text
- preset buttons:
  - `Strict (0/0/0, no new metrics)`
  - `Demo (sample pass)`
- compare-mode aware validation logic and baseline-only fallback mode when B is not loaded

### 2) Operator docs visibility
Updated:
- `support/modernizationRunbook.jsp` viewer note (mentions guided validation thresholds)
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer moved to this doc (`doc 98`)

## Test Evidence
- Browser `file://` viewer test ✅
  - threshold panel renders
  - strict/default thresholds show FAIL summary for embedded samples
  - compare mode + sample B load works
  - `Demo (sample pass)` preset flips validation summary to PASS on embedded sample A/B
  - compare-mode delta table still works (`templateMaxBetScale2` mismatch delta `+1`)
- Dashboard embedded sync ✅
  - checklist evidence path now points to `docs/98-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-163156.md`
  - summary: `PASS=38, FAIL=0, SKIP=0`

## Compatibility / Rollback
- Viewer remains a standalone support page and does not affect GS runtime behavior.
- Rollback is isolated to viewer UI logic and docs/checklist references.

## Next Step
- Add optional comparison report export (compact JSON/Markdown summary of thresholds + results) or preconfigured policy profiles for common validation scenarios (strict canary, investigative compare, regression audit).
