# Phase 8 Wave 2 Scale-Ready Helper Path (Batch 2) (2026-02-23)

## Purpose
Continue Phase 8 Wave 2 by introducing scale-ready helper paths for game-settings/coin-rule calculations while keeping the runtime path pinned to legacy scale=2 behavior.

## Scope (safe-only)
- Helper-path generalization in `DynamicCoinManager` and `GamesLevelHelper`
- Legacy behavior preserved via scale=2 delegates and constants
- No functional precision switch yet

## Code Changes
### 1) `DynamicCoinManager` scale-ready minor-unit helper path
File:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/DynamicCoinManager.java`

Added:
- `LEGACY_CURRENCY_MINOR_UNIT_SCALE = 2`
- `getBaseBetInCurrencyMinorUnitsByScale(IBaseGameInfo gameInfo, int minorUnitScale)`
- `getMinorUnitsPerCurrencyByScale(int minorUnitScale)`

Changed:
- `getLegacyBaseBetInCurrencyMinorUnits(...)` now delegates to the scale-ready helper with legacy scale `2`

Result:
- line-based base-bet normalization assumption is now explicit, scale-ready, and still runtime-compatible

### 2) `GamesLevelHelper` scale-ready minor-unit multiplier path
File:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java`

Added:
- `LEGACY_CURRENCY_MINOR_UNIT_SCALE = 2`
- `getTemplateMaxBetByMinorUnitScale(double templateMaxCredits, int minorUnitScale)`
- `getMinorUnitMultiplierByScale(int minorUnitScale)`

Changed:
- `getLegacyTemplateMaxBet(...)` now delegates to the generic helper with legacy scale `2`
- updated legacy comment to explicit scale wording

Result:
- default max-bet derivation now has a scale-ready helper path while preserving legacy behavior

## Evidence (Wave 2 progress)
- Refreshed bucket report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260223-154219.md`
- Key bucket result after batch 2:
  - `wave1_reporting_stats: 0`
  - `wave2_settings_coin_rules: 0`

## Validation
- Targeted `rg` confirms new scale-ready helper methods/constants in both files ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave2-settings-coinrule-vector-smoke.sh` ✅
  - result: `summary pass=10 fail=0`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260223-154219.md`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-154219.md`
  - summary: `PASS=32`, `FAIL=0`, `SKIP=0`

## Notes
- This batch completes Wave 2 bucket cleanup by isolating assumptions and clarifying legacy scale usage, but does not yet enable 0.001 behavior in these paths.
- Next precision behavior changes should use a dual-calculation/compare approach or feature-flagged path under the same Wave 2 vector smoke.

## Next Step
Start Phase 8 Wave 2 batch 3 with dual-calculation comparison scaffolding (legacy scale=2 vs scale-ready candidate path) in settings/coin-rule logic, still logging/guarding only and not switching behavior.
