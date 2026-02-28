# Hard-Cut M2 Wave 298 + Wave 299 Report

Date (UTC): 2026-02-28
Wave group: 298 + 299
Scope: declaration-first migration on low-fanout `sb-utils` session/util surfaces with bounded stabilization.

## Batch Breakdown
- `W298` (Batch A): retained `6` declaration migrations (`GameSessionExtendedProperties`, `GameSessionStatistics`, `IGameSession`, `IPlayerGameSettings`, `AccountIdGenerator`, `DateUtils`).
- `W299` (Batch B): retained `6` declaration migrations (`InheritFromTemplate`, `ObjectCreator`, `CookieUtils`, `DESCrypter`, `SynchroTimeProvider`, `IGeoIp`).
- Total retained declaration migrations: `12`.

## Stabilization
- Parallel target remained `1 explorer + 2 workers + main`, but explorer/worker/awaiter spawning stayed thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failure:
  - `STEP03/PRE02` compile drift in `sb-utils`: moved `SynchroTimeProvider` lost same-package visibility to unmoved `ITimeProvider` and `ExecutorUtils`.
- `rerun2` failure:
  - same `STEP03/PRE02` compile drift persisted because initial import patch did not apply.
- `rerun3` fix:
  - bounded explicit compatibility imports in moved `SynchroTimeProvider`:
    - `com.dgphoenix.casino.common.util.ITimeProvider`
    - `com.dgphoenix.casino.common.util.ExecutorUtils`
  - canonical profile reached.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-130535-hardcut-m2-wave298-wave299-session-util-lowfanout/`
- Fast gate batchA:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave checkpoint:
  - retained declaration migrations (`com.dgphoenix -> com.abs`): `12`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `444` (baseline `2277`, reduced `1833`).
- Hard-cut burndown completion: `80.500659%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `48.521990%`
  - Core total (01+02): `74.260995%`
  - Entire portfolio: `87.130498%`
- ETA refresh: ~`18.1h` (~`2.26` workdays).
