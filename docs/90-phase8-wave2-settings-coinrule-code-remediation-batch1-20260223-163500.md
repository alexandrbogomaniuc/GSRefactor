# Phase 8 Wave 2 Settings/Coin-Rule Code Remediation (Batch 1) (2026-02-23)

## Purpose
Start Phase 8 Wave 2 code remediation in game-settings/coin-rule paths with behavior-preserving refactors only, keeping legacy cent-scale assumptions explicit and isolated before later precision generalization.

## Scope (safe-only)
- `DynamicCoinManager` / `GamesLevelHelper` internal helper extraction and assumption isolation
- No precision behavior change yet
- No wallet/gameplay/session financial flow changes
- No protocol changes

## Code Changes
### 1) `DynamicCoinManager` legacy line-based normalization isolated
File:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/DynamicCoinManager.java`

Changes:
- Added explicit legacy constant:
  - `LEGACY_BASE_BET_IN_CURRENCY_MINOR_UNITS_PER_LINE = 100`
- Extracted helper methods:
  - `getLegacyBaseBetInCurrencyMinorUnits(IBaseGameInfo gameInfo)`
  - `getDefaultBetDistance(double coinValue, int baseBetInCurrency, double finalDefaultBet)`
- Replaced inline line-based normalization and delta calculation with helper calls.

Purpose:
- make the cent-scale line normalization assumption explicit and local
- prepare targeted precision migration later without mixing behavior changes now

### 2) `GamesLevelHelper` legacy max-bet default isolated
File:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java`

Changes:
- Extracted helper method:
  - `getLegacyTemplateMaxBet(double templateMaxCredits)`
- `getGLMaxBet(...)` now uses the helper instead of inline `ONE_HUNDRED_CENTS * ctx.getTemplateMaxCredits()`.

Purpose:
- isolate the legacy default cent-scale max-bet derivation behind a named helper
- enable future precision-aware replacement under test without broad method rewrites

## Evidence (Wave 2 progress)
- Refreshed bucket report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260223-153806.md`
- Bucket status after batch 1:
  - `wave1_reporting_stats: 0` (closed)
  - `wave2_settings_coin_rules: 1` (remaining explicit legacy comment in `GamesLevelHelper`)

## Validation
- Targeted grep confirms new helpers/constant usage in Wave 2 files ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave2-settings-coinrule-vector-smoke.sh` ✅
  - result: `summary pass=10 fail=0`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260223-153806.md`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-153916.md`
  - summary: `PASS=32`, `FAIL=0`, `SKIP=0`

## Notes
- Remaining Wave 2 bucket hit is intentionally retained as documentation of a legacy assumption (`GamesLevelHelper` comment) until behavior-level precision modernization is implemented.

## Next Step
Proceed to Phase 8 Wave 2 batch 2: precision-aware helper introduction for line-based normalization/default max-bet calculations (still compatibility-preserving), validated against the Wave 2 vector smoke and full verification suite.
