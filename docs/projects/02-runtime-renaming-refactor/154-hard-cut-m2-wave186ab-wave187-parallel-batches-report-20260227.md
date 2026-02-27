# Hard-Cut M2 Wave 186A/186B + Wave 187 Report

Date (UTC): 2026-02-27
Wave group: 186A + 186B + 187
Scope: declaration-first migration in `gs.managers.game.session` + `gs.managers.payment.bonus.client` with bounded importer/config rewires.

## Batch Breakdown
- `W186A`: 5 declaration migrations (`CloseGameSessionNotifyRequest`, `CloseGameSessionNotifyTask`, `CloseGameSessionNotifyTracker`, `GameSessionManager`, `INotifyResponseProcessor`).
- `W186B`: 6 declaration migrations (`BonusAccountInfoResult`, `BonusAuthResult`, `RESTBETDSIClient`, `RESTClient`, `RESTEMGetClient`, `RESTGetClient`).
- `W187`: integration and validation.

## Stabilization
- No source rollback required.
- Applied targeted importer rewires in direct Java dependents and bounded class-name rewires in `BankInfoCache.xml` profiles.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-111434-hardcut-m2-wave186ab-wave187-parallel-batches/`
- Fast gate:
  - rerun1: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `11`.
- Scoped bounded rewires retained: `19`.
- Global tracked declarations/files remaining: `1222` (baseline `2277`, reduced `1055`).
- Hard-cut burndown completion: `46.332894%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.791611%`
  - Core total (01+02): `65.395806%`
  - Entire portfolio: `82.697903%`
- ETA refresh: ~`50.5h` (~`6.32` workdays).
