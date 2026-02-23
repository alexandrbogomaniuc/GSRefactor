# Phase 8 Wave 3 Discrepancy Export Tool and Visibility (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Add a structured export visibility tool for the new discrepancy snapshot logs/counters (no GS runtime behavior change).
- Keep compare mode diagnostics opt-in and non-production oriented.

## What Changed
### 1) Structured discrepancy export parser (operator tool)
Added:
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export.sh`

Function:
- parses GS log lines containing `phase8-precision-dual-calc ...`
- aggregates by metric (`baseBetMinorUnitsScale2`, `templateMaxBetScale2`, etc.)
- exports structured JSON summary with:
  - `totalSnapshotLines`
  - `metricCount`
  - per-metric snapshot count / max-check / mismatch counters / last observed values

Safety:
- parser is read-only (post-processing logs)
- no runtime behavior change in GS

### 2) Deterministic export parser smoke test
Added:
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export-smoke.sh`

Coverage:
- synthetic log with INFO/WARN snapshot lines
- parser execution + JSON export file generation
- aggregate correctness assertions (counts, ids, mismatch summary)

### 3) Verification suite integration
Updated:
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`

Added default checks:
- CLI help: export tool + export smoke
- executable logic smoke: export parser

### 4) Operator visibility docs/runbook
Updated:
- `support/modernizationRunbook.jsp` with a Wave 3 discrepancy export command block
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer to this doc (`doc 95`)

## Test Evidence
- `bash -n gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export.sh` ✅
- `bash -n gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export-smoke.sh` ✅
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export.sh --help` ✅
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export-smoke.sh --help` ✅
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export-smoke.sh` ✅
- `gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` ✅
  - embedded checklist evidence path now points to `docs/95-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-160453.md`
  - summary: `PASS=38, FAIL=0, SKIP=0`

## Compatibility / Rollback
- No change to default GS runtime precision behavior.
- Compare mode remains opt-in (`abs.gs.phase8.precision.dualCalc.compare`).
- Rollback is isolated to parser/smoke/docs/checklist changes.

## Next Step
- Continue Phase 8 Wave 3 with optional support/admin presentation of exported discrepancy JSON (or a lightweight viewer) for non-developer validation during non-prod compare-mode runs.
