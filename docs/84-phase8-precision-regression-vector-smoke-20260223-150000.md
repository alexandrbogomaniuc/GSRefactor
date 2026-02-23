# Phase 8 Precision Regression Vector Smoke (2026-02-23)

## Purpose
Add an executable deterministic precision guard before changing GS money arithmetic, focused on the new `0.001` support requirement and line-based total calculations.

## Delivered
- New script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-regression-vector-smoke.sh`
- Verification suite integration:
  - CLI help check
  - executable logic smoke run
  - file: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
- Support runbook update (Phase 8 section includes vector smoke command):
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`

## What It Verifies (scale=3)
- exact parse of `0.001`, `0.01`, `0.3` into integer thousandth-units
- exact formatting back to fixed-scale decimals (e.g. `300 -> 0.300`)
- line-total multiplication examples (`30 * 0.001`, `30 * 0.01`, `25 * 0.004`)
- exact additive behavior across multiple wagers (`0.001 + 0.002 + 0.007`)
- rejection of over-precision inputs (`0.0009` for scale 3)
- rejection of malformed decimal strings

## Validation
- `bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-regression-vector-smoke.sh` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-regression-vector-smoke.sh --help` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-regression-vector-smoke.sh` ✅
  - result: `summary pass=10 fail=0`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-144724.md`
  - summary: `PASS=24`, `FAIL=0`, `SKIP=0`

## Why This Matters
This gives a stable acceptance baseline for future precision refactors (e.g., replacing cent-based assumptions) without touching production logic yet. It reduces the risk of introducing silent rounding regressions while migrating to `0.001`-capable paths.

## Next Step
Use the Phase 8 audit report to select the first safe remediation wave (non-financial reporting/statistics conversions vs core wallet/gameplay settlement paths), then add targeted regression vectors per code bucket before any behavior changes.
