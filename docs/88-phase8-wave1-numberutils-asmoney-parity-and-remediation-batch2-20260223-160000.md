# Phase 8 Wave 1 NumberUtils.asMoney Parity and Remediation (Batch 2) (2026-02-23)

## Purpose
Complete a second safe Wave 1 reporting/display batch by refactoring `NumberUtils.asMoney` to use centralized helper conversion while explicitly preserving legacy `Math.round` cent semantics.

## Scope (safe-only)
- Reporting/display utility path (`NumberUtils.asMoney`)
- No wallet/gameplay/session/settlement flow changes
- No protocol changes

## Code Change
File:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java`

Change:
- `asMoney(double d)` refactored from inline cent conversion:
  - old: `(double) Math.round(d * 100) / 100`
  - new: `centsToDouble(Math.round(d * 100))`

Why this is safe:
- keeps the same `Math.round`-based cent rounding behavior
- centralizes the final cent-to-double conversion through the Wave 1 helper added in batch 1
- avoids changing negative half-cent behavior (still legacy semantics)

## New Acceptance Guard
Added deterministic parity smoke for `NumberUtils.asMoney` legacy behavior:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave1-numberutils-asmoney-parity-smoke.sh`

Coverage includes:
- positive/negative cent-boundary values (`0.004`, `0.005`, `-0.004`, `-0.005`, `-0.006`)
- larger values
- explicit legacy edge-case check (`-0.005 -> 0.00` under `Math.round` semantics)
- parity between legacy expression and refactored helper-based expression

## Verification Suite Integration
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - added CLI help + executable logic smoke checks for the new `asMoney` parity script

## Validation
- `bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave1-numberutils-asmoney-parity-smoke.sh` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave1-numberutils-asmoney-parity-smoke.sh` ✅
  - result: `summary pass=12 fail=0`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave1-reporting-vector-smoke.sh` ✅
  - result: `summary pass=12 fail=0`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-152819.md`
  - summary: `PASS=30`, `FAIL=0`, `SKIP=0`

## Notes
- This batch intentionally preserves legacy `Math.round` negative tie behavior. Any future move to `BigDecimal`/explicit `HALF_UP` for `asMoney` requires a separate parity decision and broader caller audit.

## Next Step
Continue Wave 1 with remaining low-risk display/reporting standardization only, or conclude Wave 1 and start Wave 2 planning for game-settings/coin-rule assumptions under new precision vectors.
