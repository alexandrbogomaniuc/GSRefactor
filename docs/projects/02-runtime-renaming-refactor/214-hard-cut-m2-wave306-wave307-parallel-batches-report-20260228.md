# Hard-Cut M2 Wave 306 + Wave 307 Report

Date (UTC): 2026-02-28
Wave group: 306 + 307
Scope: declaration-first migration on mixed low-fanout `sb-utils` `currency/exception/web-bonus/util/cache/wallet-errors` with bounded compatibility stabilization.

## Batch Breakdown
- `W306` (Batch A): retained `5` declaration migrations (`CurrencyRate`, `ICurrencyRateManager`, `BonusException`, `BonusError`, `CommonWalletErrors`).
- `W307` (Batch B): retained `5` declaration migrations (`ReflectionUtils`, `DigitFormatter`, `KryoHelper`, `JsonSelfSerializable`, `CacheKeyInfo`).
- Deferred from initial target: `0`.
- Total retained declaration migrations: `10`.

## Stabilization
- Parallel target remained `1 explorer + 2 workers + main`, but explorer/worker/awaiter spawning stayed thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1-rerun5`: resolved compile-order and mixed package-boundary failures across `STEP01/STEP05`.
- `rerun6-rerun10`: resolved `STEP06` class/exception/type drift with bounded compatibility fixes in wallet and bonus/currency boundaries.
- `rerun11`: reached canonical validation profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-142644-hardcut-m2-wave306-wave307-mixed-lowfanout-coreutils/`
- Fast gate batchA:
  - rerun11: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun11: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun11: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave checkpoint:
  - retained declaration migrations (`com.dgphoenix -> com.abs`): `10`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+10`
- Global tracked declarations/files remaining: `2108` (baseline `2277`, reduced `169`).
- Hard-cut burndown completion: `7.422047%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `25.927756%`
  - Core total (01+02): `62.963878%`
  - Entire portfolio: `81.481939%`
- ETA refresh: ~`96.8h` (~`12.10` workdays).
