# Phase 8 Wave 3 Imported Report Diff Filters and Drag/Drop Import UX (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Refine compact compare-report artifact review UX in the viewer.
- No GS runtime/protocol behavior change.

## What Changed
### 1) Changed-only filters for imported artifact diff mode
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

Added imported diff filters:
- `Show changed rules only` (default: on)
- `Show changed metrics only` (default: on)

Behavior:
- filters are local to browser session and apply to the imported artifact diff card (`A vs B`)
- diff metadata/footers now show changed vs visible counts
- unchanged rows can be shown for full artifact inspection by turning filters off

### 2) Drag/drop import UX for compact compare-report JSON
Added drag/drop support onto Imported A/B textareas:
- drop a `.json` file (uses first file in drop payload), or
- drop plain JSON text

Behavior:
- works for both Imported A and Imported B slots
- drop target highlight shown while dragging over textarea
- imported source metadata records drop path (`drop:A:*` / `drop:B:*`)

## Test Evidence
- Browser `file://` viewer test ✅
  - imported artifact diff mode renders for Imported A/B compact compare reports
  - changed-only filters toggle rows/visibility counts (metrics filter verified `0 -> 3` visible rows)
  - synthetic drop-path test via `handleCompareReportDrop(..., 'B')` imports compact compare JSON text into Imported B and re-renders diff mode
- Dashboard embedded sync ✅
  - checklist evidence path points to `docs/105-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-171407.md`

## Compatibility / Rollback
- Viewer UX refinement is additive and client-side only; no GS runtime behavior or protocol behavior changed.
- Rollback is isolated to support page UI logic and documentation/checklist references.

## Next Step
- Add imported artifact diff filters for rules by status class (PASS/FAIL/INFO changes) and metric search/filter by metric name for faster operator triage.
