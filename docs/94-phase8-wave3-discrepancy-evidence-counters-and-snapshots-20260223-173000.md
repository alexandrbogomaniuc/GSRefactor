# Phase 8 Wave 3 Discrepancy Evidence Counters and Snapshots (2026-02-23)

## Scope
- GS-only Phase 8 Wave 3 continuation (main project priority).
- Add disabled-by-default discrepancy evidence collection scaffolding to existing parity hooks in settings/coin-rule paths.
- No runtime precision behavior switch.

## What Changed
### 1) GS parity hooks now collect evidence (when enabled)
Updated classes:
- `common-gs/.../DynamicCoinManager.java`
- `common-gs/.../GamesLevelHelper.java`

Added (disabled by default unless `-Dabs.gs.phase8.precision.dualCalc.compare=true`):
- parity check counters (`checkCount`, `mismatchCount`) via `AtomicLong`
- throttled info snapshots (`first check`, then every `N` checks)
- immediate warn snapshot on mismatch before existing exception
- configurable snapshot interval:
  - `abs.gs.phase8.precision.dualCalc.logEvery`
  - invalid/blank/non-positive values fall back to safe default (`1000`)

Behavioral safety:
- default behavior unchanged (comparison disabled)
- when comparison is enabled, existing fail-fast mismatch exception remains in place

### 2) Executable smoke for discrepancy evidence scaffold
Added script:
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-evidence-smoke.sh`

Coverage:
- `logEvery` normalization/fallback
- snapshot throttle rules (first, interval, mismatch)
- snapshot message shape for both Wave 3 hook locations
- counter progression example

### 3) Verification suite integration
- Added CLI help + executable smoke checks for the new discrepancy evidence scaffold into:
  - `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`

## Test Evidence
- `bash -n gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-evidence-smoke.sh` ✅
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-evidence-smoke.sh --help` ✅
- `gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-evidence-smoke.sh` ✅ (`summary pass=12 fail=0`)
- `rg` callsite checks confirm Wave 3 counters/snapshot methods in both GS classes ✅
- `gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` ✅ (embedded snapshot metadata refreshed)
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-160001.md`
  - summary: `PASS=36, FAIL=0, SKIP=0`

## Compatibility / Rollback
- No production behavior change by default.
- Rollback is isolated to Wave 3 scaffolding:
  - remove counters/snapshot logging helpers from parity hooks,
  - remove new smoke script and suite wiring,
  - restore prior checklist evidence pointer.

## Next Step
- Continue Phase 8 Wave 3 with opt-in discrepancy reporting aggregation (structured snapshot export or admin/support visibility) while keeping comparison disabled by default and preserving fail-fast behavior when comparison mode is enabled.
