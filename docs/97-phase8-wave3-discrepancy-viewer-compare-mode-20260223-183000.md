# Phase 8 Wave 3 Discrepancy Viewer Compare Mode (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Extend the support viewer to compare two discrepancy export JSON files (A vs B) for non-prod validation.
- No GS runtime behavior change; compare mode diagnostics remain opt-in.

## What Changed
### 1) Viewer compare mode (A vs B)
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

New capabilities:
- dual inputs: baseline JSON A + comparison JSON B
- two file uploads (`A`, `B`) and two embedded samples (`A`, `B`)
- compare toggle (`Compare A vs B`)
- comparison summary cards:
  - delta snapshot lines
  - delta metric count
  - delta mismatch metrics
  - changed mismatch metrics
  - union metric count
- metric table deltas:
  - `B Max Mismatch`
  - `Delta (Mismatch)`
  - `Delta (Snapshots)`
- mismatch-only filter now works across both A and B metrics
- compare-only metrics (present only in B) are rendered and visually marked

### 2) Operator docs visibility
Updated:
- `support/modernizationRunbook.jsp` (viewer note now mentions compare mode)
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer moved to this doc (`doc 97`)

## Test Evidence
- Browser `file://` viewer test ✅
  - baseline sample A auto-load works
  - sample B load works
  - compare toggle works
  - mismatch-only filter works in compare mode
  - comparison summary renders deltas (sample A vs B)
  - metric delta row example visible (`templateMaxBetScale2`: mismatch delta `+1`, snapshots delta `+1`)
- Dashboard embedded sync ✅
  - checklist evidence path now points to `docs/97-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-162641.md`
  - summary: `PASS=38, FAIL=0, SKIP=0`

## Compatibility / Rollback
- Viewer remains a standalone support page; no GS runtime or protocol behavior changes.
- Rollback is isolated to the viewer HTML and docs/checklist references.

## Next Step
- Add an optional guided operator validation checklist inside the viewer (e.g., expected zero mismatch metrics for baseline, mismatch delta thresholds) or export a compact comparison report JSON/Markdown from the viewer/parser toolchain.
