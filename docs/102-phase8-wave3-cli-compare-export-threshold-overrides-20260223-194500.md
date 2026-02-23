# Phase 8 Wave 3 CLI Compare Export Threshold Overrides (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Extend non-UI discrepancy compare/export CLI with per-run threshold overrides seeded from a named policy profile.
- No GS runtime/protocol behavior change.

## What Changed
### 1) CLI threshold override support (seeded from named policy)
Updated script:
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-compare-export.sh`

Added optional override flags:
- `--threshold-mismatch-a N`
- `--threshold-mismatch-b N`
- `--threshold-mismatch-delta N`
- `--threshold-snapshot-delta N`
- `--allow-new-metrics-in-b true|false`

Behavior:
- named policy remains the seed profile (`thresholds.profile` / `profileLabel` unchanged)
- specified flags override only selected threshold fields
- report includes `thresholds.overridesApplied` metadata
- CLI summary line now includes override count (`overrides=N`)

### 2) Deterministic override-path test coverage
Updated smoke:
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-compare-export-smoke.sh`

New smoke coverage validates:
- strict policy still fails on sample A/B
- demo policy still passes
- strict policy + overrides can pass while preserving seed profile identity
- override metadata and values are present in JSON output

### 3) Operator docs / evidence routing
Updated:
- `support/modernizationRunbook.jsp` (CLI compare/export note mentions optional per-run overrides)
- `support/modernizationDocs.jsp` Phase 8 doc list
- checklist evidence pointer moved to this doc (`doc 102`)

## Test Evidence
- Targeted script checks ✅
  - `bash -n` on compare/export CLI + smoke
  - `phase8-precision-wave3-discrepancy-compare-export.sh --help`
  - `phase8-precision-wave3-discrepancy-compare-export-smoke.sh` (strict FAIL + demo PASS + strict-with-overrides PASS)
- Dashboard embedded sync ✅
  - checklist evidence path points to `docs/102-...`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-165236.md`

## Compatibility / Rollback
- CLI compare/export remains additive tooling only; GS runtime behavior and protocol behavior are unchanged.
- Rollback is isolated to CLI scripts, smoke coverage, and documentation/checklist references.

## Next Step
- Add viewer import support for CLI compare/export JSON (same compact report format) so operators can inspect CLI-generated artifacts in the existing browser UI.
