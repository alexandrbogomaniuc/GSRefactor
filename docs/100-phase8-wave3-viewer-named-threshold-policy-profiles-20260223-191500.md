# Phase 8 Wave 3 Viewer Named Threshold Policy Profiles (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Add reusable named threshold policy profiles inside the discrepancy viewer compare workflow.
- Keep all changes client-side only (no GS runtime/protocol behavior change).

## What Changed
### 1) Named threshold policy profiles in viewer
Updated support page:
- `gs-server/game-server/web-gs/src/main/webapp/support/phase8DiscrepancyViewer.html`

Added reusable threshold policy profiles for guided validation:
- `Strict Gate`
- `Canary Gate`
- `Shadow Observe`
- `Demo Sample Pass`
- `Custom (manual)` fallback state when the operator edits threshold inputs directly

Behavior:
- profile selector + `Apply Policy` button populate threshold fields and checkbox
- existing Strict/Demo buttons now reuse the same profile-application path
- profile description is shown in the UI (`thresholdProfileMeta`)
- manual threshold edits automatically switch the selector to `Custom (manual)`
- compact comparison JSON/Markdown exports now include `thresholds.profile`

### 2) Operator docs / evidence routing
Updated:
- `support/modernizationRunbook.jsp` viewer note (mentions named threshold policy profiles)
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer moved to this doc (`doc 100`)

## Test Evidence
- Browser `file://` viewer test ✅
  - sample A + sample B + compare mode + `Canary Gate` / `Shadow Observe` / `Demo Sample Pass` profiles apply correctly
  - manual threshold edit flips selector to `Custom (manual)`
  - demo profile yields PASS validation for embedded sample A/B
  - compact comparison JSON/Markdown export previews remain populated
  - JSON preview contains `thresholds.profile`
- Dashboard embedded sync ✅
  - checklist evidence path points to `docs/100-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-164203.md`

## Compatibility / Rollback
- Viewer remains a standalone support page in `file://` mode; no runtime integrations changed.
- Rollback is isolated to the viewer page and documentation/checklist references.

## Next Step
- Add parser-side compact comparison CLI export (non-UI automation path) using the same threshold policy names, or persist local viewer policy presets in browser storage for operator reuse.
