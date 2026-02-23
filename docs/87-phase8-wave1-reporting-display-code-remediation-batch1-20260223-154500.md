# Phase 8 Wave 1 Reporting/Display Code Remediation (Batch 1) (2026-02-23)

## Purpose
Implement the first safe Phase 8 code changes only in reporting/display paths, using the Wave 1 hotspot bucket and vector smoke as acceptance guards.

## Scope (safe-only)
- Reporting/display conversions and score rounding
- No wallet/gameplay/session/settlement amount path changes
- No protocol or integration contract changes

## Code Changes
### 1) Centralized reporting/display conversion helpers in `NumberUtils`
File:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java`

Added helpers (backward-compatible, existing methods preserved):
- `centsToDouble(long cents)`
- `minorUnitsToDouble(long minorUnits, int scale)`
- `decimalStringToScaledLongHalfUp(String value, int scale)`
- `decimalStringToCentsHalfUp(String value)`

Purpose:
- remove scattered inline `/100.0d` and `Math.round(Double.parseDouble(...) * 100)` patterns from Wave 1 classes
- make rounding/conversion semantics explicit and reusable for future precision modernization

### 2) Tournament leaderboard score rounding moved to explicit helper
File:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/promo/tournaments/TournamentLeaderboardBuilder.java`

Changes:
- replaced raw `Math.round(Double.parseDouble(player.getScore()) * 100)` with `NumberUtils.decimalStringToCentsHalfUp(player.getScore())` (both callsites)

Impact:
- reporting-only path now uses explicit decimal-string HALF_UP cent rounding
- behavior remains in Wave 1 (non-financial leaderboard display/placement score formatting)

### 3) Admin/support coin display conversions centralized
Files:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/common/EditGameAction.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/common/InputModeAction.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/common/LoadGameInfoAction.java`

Changes:
- replaced direct `coin.getValue() / 100.0d` conversions with `NumberUtils.centsToDouble(coin.getValue())`

Impact:
- no behavior change intended; conversion logic is centralized to prepare future configurable precision for UI/reporting layers

## Validation
- `rg` callsite checks confirm helper usage in all targeted Wave 1 classes ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave1-reporting-vector-smoke.sh` ✅
  - result: `summary pass=12 fail=0`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-150813.md`
  - summary: `PASS=28`, `FAIL=0`, `SKIP=0`

## Notes
- This batch intentionally does not alter `NumberUtils.asMoney(double)` behavior yet (broader caller surface and negative-rounding parity risk). That remains a later Wave 1/4 decision with explicit parity review.

## Next Step
Proceed with the next Wave 1 reporting/display-only batch (e.g., `NumberUtils.asMoney` parity analysis and/or additional support UI display paths), guarded by the Wave 1 vector smoke and the shared local verification suite.
