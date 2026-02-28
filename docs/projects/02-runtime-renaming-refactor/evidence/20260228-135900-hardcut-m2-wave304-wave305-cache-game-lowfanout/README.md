# Evidence Summary: Hard-Cut M2 Wave 304 + 305

- Timestamp (UTC): `2026-02-28 13:59-14:20`
- Scope: declaration-first migration of low-fanout `sb-utils` `cache/game/util` surfaces with bounded compatibility stabilization.

## Initial Targets (10)
- `AbstractDistributedCache`
- `TransportException`
- `ImmutableBaseGameInfoWrapper`
- `ILimit`
- `GameType`
- `GameGroup`
- `GameVariableType`
- `ServerMessage`
- `DatePeriod`
- `CalendarUtils`

## Retained Targets (4)
- `TransportException`
- `ImmutableBaseGameInfoWrapper`
- `DatePeriod`
- `CalendarUtils`

## Deferred Targets (6)
- `AbstractDistributedCache`
- `ILimit`
- `GameType`
- `GameGroup`
- `GameVariableType`
- `ServerMessage`

## Rerun Timeline
- `rerun1`: failed at `PRE02/STEP03` (`sb-utils`) due moved `AbstractDistributedCache`/`TransportException`/`ImmutableBaseGameInfoWrapper` losing same-package visibility to unmoved declarations.
- `rerun2`: failed at `PRE02/STEP03` on `GameType` duplicate-type drift (`com.abs` vs legacy same-package usage).
- `rerun3`-`rerun4`: failed at `PRE02/STEP03` on `Html5PcVersionMode` + `GameType/GameGroup/GameVariableType` mixed package drift around `ImmutableBaseGameInfoWrapper`/`IBaseGameInfo`.
- `rerun5`: reached `PRE02 PASS`, then failed at `PRE03/STEP04` because moved `ServerMessage` lost protected-field access boundaries in promo responses; parallel `STEP01` `Html5PcVersionMode` boundary mismatch surfaced.
- `rerun6`: prewarm passed, but `STEP01` still failed on `ShellDetector` enum package mismatch (`com.abs` vs `com.dgphoenix`).
- `rerun7`: `STEP01-05 PASS`, then failed at `STEP06` on `DatePeriod` type mismatch in `MQServiceHandler`.
- `rerun8`: after bounded deferrals + compatibility bridges, canonical profile reached.

## Canonical Validation Outcomes (`rerun8`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
