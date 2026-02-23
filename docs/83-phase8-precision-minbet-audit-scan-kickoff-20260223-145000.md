# Phase 8 Precision / Min-Bet Audit Scan Kickoff (2026-02-23)

## Purpose
Start Phase 8 with a repeatable GS-only audit that identifies hardcoded minimum bet assumptions, decimal/rounding hotspots, and money-precision risks before remediation waves.

## Scope
- GS-only (`game-server` + `refactor-services`)
- Backward-compatible audit only (no runtime behavior changes)
- Supports Phase 8 target of `0.001` where business/wallet/currency constraints allow

## Delivered
- New scanner script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-minbet-audit-scan.sh`
- Verification suite gate added (CLI help check):
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
- Checklist/portal tracking updated:
  - `pu-precision-audit` moved to `in_progress`
  - support docs index and runbook include Phase 8 audit command

## Latest Audit Evidence
- Generated report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-minbet-audit-20260223-144425.md`
- Summary counts from report:
  - min/line keyword hits: `920`
  - numeric literal hits (`0.01`, `0.1`, `1.00`, `100`): `789`
  - rounding/decimal API hits: `231`
  - bet model term hits: `4151`
  - Java `float`/`double` declarations: `420`

## Initial Hotspot Signals (examples)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/socket/mq/MQServiceHandler.java`
  - `minBet` defaults and coin-derived min/max logic in reporting/statistics paths.
  - `Math.round(... * 100)` patterns indicate cent-based assumptions.
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GameSettingsManager.java`
  - FRB coin fallback message references `0.01`; dynamic coin conversion includes `/ 100.0` assumption.
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/DynamicCoinManager.java`
  - line-count * `100` base bet normalization suggests fixed cent precision assumptions.
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
  - static bank coin/minValue data must be reviewed for 0.001-capable profiles and compatibility constraints.

## Validation
- `bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-minbet-audit-scan.sh` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-minbet-audit-scan.sh --help` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-minbet-audit-scan.sh` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - Report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-144425.md`
  - Summary: `PASS=22`, `FAIL=0`, `SKIP=0`

## Notes
- The dashboard completion bar remains `26/41` because this increment moves Phase 8 from `planned` to `in_progress` (not `done`). The visible change is the item status/evidence in the checklist section.

## Next Step
Create Phase 8 remediation wave plan with prioritized code buckets (reporting/statistics vs gameplay/wallet-sensitive paths) and introduce executable precision regression tests before changing money arithmetic.
