# Hard-Cut M2 Wave 292 + Wave 293 Report

Date (UTC): 2026-02-28
Wave group: 292 + 293
Scope: declaration-first migration for `sb-utils/common/util` sequencer/id-generator cluster with bounded compatibility stabilization.

## Batch Breakdown
- `W292` (Batch A): `6` declaration migrations retained (`IIntegerIdGenerator`, `IIntegerSequencer`, `IIntegerSequencerPersister`, `ILongIdGenerator`, `ISequencer`, `ISequencerPersister`).
- `W293` (Batch B): `5` declaration migrations retained (`IntegerIdGenerator`, `IntegerSequencer`, `LongIdGenerator`, `LongIdGeneratorFactory`, `Sequencer`).
- Total retained declaration migrations: `11`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failed at `PRE02/STEP03` because moved `IntegerSequencer` and `Sequencer` lost same-package visibility to unmoved `ExecutorUtils`.
  - Fixed with bounded compatibility imports:
    - `com.dgphoenix.casino.common.util.ExecutorUtils` in moved `IntegerSequencer`
    - `com.dgphoenix.casino.common.util.ExecutorUtils` in moved `Sequencer`
- `rerun2` failed at `STEP06` due duplicate-type compatibility drift between moved `sb-utils` sequencer/id-generator declarations and unmoved `gs-server/common` duplicate declarations after initial usage rewires.
- Bounded stabilization rollback removed usage rewires for this class cluster (declaration migration retained), then reran full matrix.
- `rerun3` reached canonical validation profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-120911-hardcut-m2-wave292-wave293-sequencer-idgen-cluster/`
- Fast gate batchA:
  - rerun1: `STEP03 FAIL`
  - rerun2: `STEP06 FAIL`
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1: `STEP03 FAIL`
  - rerun2: `STEP06 FAIL`
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE02 FAIL`
  - rerun2: `STEP06 FAIL`
  - rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `11`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `473` (baseline `2277`, reduced `1804`).
- Hard-cut burndown completion: `79.227053%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `48.129606%`
  - Core total (01+02): `74.064803%`
  - Entire portfolio: `87.032402%`
- ETA refresh: ~`19.2h` (~`2.40` workdays).
