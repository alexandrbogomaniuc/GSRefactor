# Hard-Cut M2 Wave 274 + Wave 275 Report

Date (UTC): 2026-02-28
Wave group: 274 + 275
Scope: declaration-first migration of low-risk `common.util` enums/beans/pairs/domain helper declarations with bounded consumer rewires.

## Batch Breakdown
- `W274` (Batch A): `10` declaration migrations retained.
- `W275` (Batch B): bounded import/consumer rewires + validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Stabilization reruns:
  - `rerun1`: `STEP01`/`PRE01` failed because moved `RefererDomains` lost same-package visibility to unmigrated `CollectionUtils`.
  - fixed with explicit compatibility import: `import com.dgphoenix.casino.common.util.CollectionUtils;`.
  - after canonical `rerun2`, one wildcard-import consumer (`CassandraGameSessionPersister`) was explicitly rewired to `com.abs` `LongPair` to avoid stale classpath masking; revalidated on `rerun3`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-085357-hardcut-m2-wave274-wave275-common-util-enums-beans/`
- Fast gate batchA:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `10`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+10`
- Global tracked declarations/files remaining: `530` (baseline `2277`, reduced `1747`).
- Hard-cut burndown completion: `76.723759%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.358402%`
  - Core total (01+02): `73.679201%`
  - Entire portfolio: `86.839600%`
- ETA refresh: ~`21.7h` (~`2.71` workdays).
