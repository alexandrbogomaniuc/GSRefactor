# Hard-Cut M2 Wave 290 + Wave 291 Report

Date (UTC): 2026-02-28
Wave group: 290 + 291
Scope: declaration-first migration for low-risk `common.util` low-fanout surfaces with bounded usage rewires.

## Batch Breakdown
- `W290` (Batch A): `2` declaration migrations retained (`NtpSyncInfo`, `LookAheadReader`).
- `W291` (Batch B): `2` declaration migrations retained (`RSACrypter`, `ZipUtils`).
- Total retained declaration migrations: `4`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- No compile/package stabilization rewires were required beyond bounded usage rewires for moved util classes.
- Bounded import rewires applied:
  - `EncoderAction`: `com.dgphoenix.casino.common.util.ZipUtils` -> `com.abs.casino.common.util.ZipUtils`
  - `SessionKeyAccessAction`: `com.dgphoenix.casino.common.util.RSACrypter` -> `com.abs.casino.common.util.RSACrypter`
- `rerun1` reached canonical validation profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-115455-hardcut-m2-wave290-wave291-common-util-lowfanout/`
- Fast gate batchA:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `4`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+4`
- Global tracked declarations/files remaining: `484` (baseline `2277`, reduced `1793`).
- Hard-cut burndown completion: `78.743961%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.980777%`
  - Core total (01+02): `73.990388%`
  - Entire portfolio: `86.995194%`
- ETA refresh: ~`19.8h` (~`2.48` workdays).
