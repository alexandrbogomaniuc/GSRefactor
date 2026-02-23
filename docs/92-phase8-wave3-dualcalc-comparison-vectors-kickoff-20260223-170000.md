# Phase 8 Wave 3 Dual-Calculation Comparison Vectors Kickoff (2026-02-23)

## Scope
- GS-only precision modernization (Phase 8).
- No runtime behavior switch.
- Add deterministic offline comparison guard for legacy scale=2 vs generalized precision helper path.

## What Was Added
- New executable smoke script:
  - `gs-server/deploy/scripts/phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh`
- Verification suite integration:
  - CLI help check
  - executable logic smoke check

## Coverage (Wave 3 comparison scaffold)
- Legacy scale=2 parity checks for:
  - currency multiplier
  - base bet normalization by line count
  - template max-bet parsing
  - total bet calculation for coin/lines
  - nearest-coin selection
- Deterministic scale=3 comparison-only checks for future enablement:
  - `0.001` coin support math examples
  - scale=3 max-bet parsing
  - nearest-coin selection examples
- Delta visibility checks:
  - legacy scale=2 rejection of `0.001`
  - generalized scale=3 path supports same case deterministically

## Test Evidence
- `bash -n gs-server/deploy/scripts/phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh` ✅
- `gs-server/deploy/scripts/phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh --help` ✅
- `gs-server/deploy/scripts/phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh` ✅ (`summary pass=12 fail=0`)
- `gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` ✅ (`embedded-checklist synced: 26/41`)
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-154648.md`
  - summary: `PASS=34, FAIL=0, SKIP=0`

## Compatibility / Rollback
- Backward compatibility preserved: no GS runtime precision behavior change.
- Rollback is trivial: remove Wave 3 smoke script + suite wiring + checklist/docs references.

## Next Step
- Start Phase 8 Wave 3 code scaffolding for optional dual-calculation comparison hooks in GS settings/coin-rule paths (disabled by default), then gather discrepancy evidence before any feature flag behavior switch.
