# Hard-Cut M2 Wave 302 + Wave 303 Report

Date (UTC): 2026-02-28
Wave group: 302 + 303
Scope: declaration-first migration on low-fanout `sb-utils` `xmlwriter/logkit/statistics` surfaces with bounded compile-order stabilization.

## Batch Breakdown
- `W302` (Batch A): retained `5` declaration migrations (`GameLog`, `LogUtils`, `ThreadLog`, `IStatisticsGetter`, `IntervalStatistics`).
- `W303` (Batch B): retained `4` declaration migrations (`Attribute`, `FormattedXmlWriter`, `XmlQuota`, `XmlWriter`).
- Deferred from initial target: `1` (`PromoWinInfo`).
- Total retained declaration migrations: `9`.

## Stabilization
- Parallel target remained `1 explorer + 2 workers + main`, but explorer/worker/awaiter spawning stayed thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failure:
  - `PRE01` compile drift in `gs-server/utils`: pre-step modules imported moved `GameLog` before `sb-utils` install.
- `rerun2` failure:
  - after bounded pre-step rollback, `STEP06` mixed-type wallet boundary on `PromoWinInfo` (`com.dgphoenix` vs `com.abs`).
- `rerun3` fixes:
  - bounded rollback of rewires in pre-step modules (`utils/common/common-wallet`) for compile-order safety.
  - deferred `PromoWinInfo` from this wave (rollback declaration move + rewires).
  - added bounded compatibility imports in unmoved `StatisticsManager` for moved `IStatisticsGetter`/`IntervalStatistics`.
  - canonical profile reached.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-134248-hardcut-m2-wave302-wave303-xmlwriter-logkit-stats-promo/`
- Fast gate batchA:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave checkpoint:
  - retained declaration migrations (`com.dgphoenix -> com.abs`): `9`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+9`
- Global tracked declarations/files remaining: `427` (baseline `2277`, reduced `1850`).
- Hard-cut burndown completion: `81.247255%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `48.752011%`
  - Core total (01+02): `74.376005%`
  - Entire portfolio: `87.188003%`
- ETA refresh: ~`17.4h` (~`2.18` workdays).
