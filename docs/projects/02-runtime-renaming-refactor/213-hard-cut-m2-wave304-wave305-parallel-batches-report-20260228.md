# Hard-Cut M2 Wave 304 + Wave 305 Report

Date (UTC): 2026-02-28
Wave group: 304 + 305
Scope: declaration-first migration on low-fanout `sb-utils` `cache/game/util` surfaces with bounded overlap-safe stabilization.

## Batch Breakdown
- `W304` (Batch A): retained `2` declaration migrations (`TransportException`, `ImmutableBaseGameInfoWrapper`).
- `W305` (Batch B): retained `2` declaration migrations (`DatePeriod`, `CalendarUtils`).
- Deferred from initial target: `6` (`AbstractDistributedCache`, `ILimit`, `GameType`, `GameGroup`, `GameVariableType`, `ServerMessage`).
- Total retained declaration migrations: `4`.

## Stabilization
- Parallel target remained `1 explorer + 2 workers + main`, but explorer/worker/awaiter spawning stayed thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` (`PRE02/STEP03`) failed on moved declarations losing same-package visibility to unmoved classes.
- `rerun2` (`PRE02/STEP03`) failed on `GameType` duplicate-type drift; this triggered bounded defer of the `game/bank` cluster.
- `rerun3`-`rerun4` (`PRE02/STEP03`) failed on mixed moved/unmoved `Html5PcVersionMode` and `GameType/GameGroup/GameVariableType` boundaries.
- `rerun5` reached `PRE02 PASS` then failed:
  - `PRE03/STEP04`: moved `ServerMessage` broke protected-field access across promo packages.
  - `STEP01`: `Html5PcVersionMode` package mismatch in `ShellDetector` (`com.abs` vs `com.dgphoenix`).
- `rerun6`: prewarm green; `STEP01` still failed on enum package mismatch.
- `rerun7`: `STEP01-05 PASS`; `STEP06` failed on `DatePeriod` type mismatch in `MQServiceHandler`.
- `rerun8` fixes and outcome:
  - deferred/rolled back declaration moves for `AbstractDistributedCache`, `ILimit`, `GameType`, `GameGroup`, `GameVariableType`, `ServerMessage`.
  - retained moved declarations plus bounded compatibility imports/bridges:
    - `IBaseGameInfo` imports moved `Html5PcVersionMode`/`ClientGeneration`.
    - `ShellDetector` uses legacy enum bridge (`asLegacyMode`) for moved interface return type.
    - `MQServiceHandler` uses moved `com.abs` `DatePeriod` type at boundary callsite.
  - canonical profile reached.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-135900-hardcut-m2-wave304-wave305-cache-game-lowfanout/`
- Fast gate batchA:
  - rerun8: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun8: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun8: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave checkpoint:
  - retained declaration migrations (`com.dgphoenix -> com.abs`): `4`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+4`
- Global tracked declarations/files remaining: `423` (baseline `2277`, reduced `1854`).
- Hard-cut burndown completion: `81.422925%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `48.806134%`
  - Core total (01+02): `74.403067%`
  - Entire portfolio: `87.201533%`
- ETA refresh: ~`17.2h` (~`2.15` workdays).
