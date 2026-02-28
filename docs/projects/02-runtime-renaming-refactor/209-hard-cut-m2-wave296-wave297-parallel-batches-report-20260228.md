# Hard-Cut M2 Wave 296 + Wave 297 Report

Date (UTC): 2026-02-28
Wave group: 296 + 297
Scope: declaration-first migration on low-fanout `sb-utils` cache/game/lock/util surfaces with bounded stabilization.

## Batch Breakdown
- `W296` (Batch A): retained `6` declaration migrations (`JsonDeserializableDeserializer`, `JsonDeserializableModule`, `UniversalCollectionModule`, `ClientGeneration`, `Html5PcVersionMode`, `ServerLockInfo`).
- `W297` (Batch B): retained `6` declaration migrations (`ChangeLockListener`, `BidirectionalMultivalueMap`, `ConcurrentBidirectionalMap`, `EnumMapSerializer`, `FastByteArrayOutputStream`, `Controllable`).
- Total retained declaration migrations: `12`.

## Stabilization
- Parallel target remained `1 explorer + 2 workers + main`, but explorer/worker/awaiter spawning stayed thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failure:
  - `STEP01/PRE01` compile drift from external-module rewires to moved `com.abs` classes before `sb-utils` compile/install order.
  - fix: rollback external usage rewires; keep declaration migration.
- `rerun2` failure:
  - `STEP03/PRE02` compile drift in `sb-utils` (`ConcurrentBidirectionalMap` duplicate/cannot-access) from over-rollback of same-module imports.
  - fix: keep `com.abs` rewires for in-module `sb-utils` consumers (`Configuration`, `IEngine`, `LockInfo`, `AbstractSocketClient`) while external modules remain on legacy imports.
- `rerun3` reached canonical profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-124659-hardcut-m2-wave296-wave297-cache-util-lowfanout/`
- Fast gate batchA:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave checkpoint:
  - `com.dgphoenix -> com.abs`: `12`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+12`
- Global tracked declarations/files remaining: `455` (baseline `2277`, reduced `1822`).
- Hard-cut burndown completion: `80.017567%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `48.373153%`
  - Core total (01+02): `74.186577%`
  - Entire portfolio: `87.093288%`
- ETA refresh: ~`18.5h` (~`2.31` workdays).
