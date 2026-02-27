# Hard-Cut M2 Wave 206A/206B + Wave 207 Report

Date (UTC): 2026-02-27
Wave group: 206A + 206B + 207
Scope: declaration-first migration in promo core/common-gs promo surfaces plus `sb-utils` `common.configuration` and `common.engine` surfaces.

## Batch Breakdown
- `W206A`: 16 declaration migrations across `common-gs` promo classes and `promo/core` interfaces/services.
- `W206B`: 10 declaration migrations across `sb-utils` `common.configuration` and `common.engine` classes.
- `W207`: integration and validation.

## Stabilization
- Primary mode executed as `1 explorer + 2 workers + main` with strict non-overlapping ownership.
- Zero-overlap selection validated up-front (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires retained only in direct importer/FQCN call-sites listed in `rewires-batchA-all.txt` and `rewires-batchB-all.txt`.
- Fixed three incorrect import rewires in `GameServerComponentsConfiguration` (`KafkaRequestMultiPlayer`, `TournamentMessageHandlersFactory`, `GameServerConfiguration`) back to their actual declaration packages.
- Resolved mixed `ConfigHelper` type mismatch in `CassandraPersistenceContextConfiguration` for `KeyspaceConfigurationFactory` constructor compatibility.
- Warm-installed promo dependencies before canonical rerun (`promo/persisters`, `promo/core`).
- Corrected validation runner `STEP08` path to `mp-server/persistance` for this repository layout.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) out of migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-155735-hardcut-m2-wave206ab-wave207-parallel-batches/`
- Fast gate:
  - rerun1: `STEP01 FAIL` (`rc=1`)
  - rerun2: `STEP06 FAIL` (`rc=1`)
  - rerun3: `STEP06 FAIL` (`rc=1`, `ConfigHelper` type mismatch)
  - rerun4: `STEP08 FAIL` (`rc=1`, runner path mismatch)
  - rerun5 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias)
- Full matrix:
  - rerun1: `PRE01 FAIL` (`rc=1`)
  - rerun2: `PRE01-03 PASS`, `STEP01-05 PASS`, `STEP06 FAIL` (`rc=1`)
  - rerun3: `PRE01-03 PASS`, `STEP01-05 PASS`, `STEP06 FAIL` (`rc=1`, `ConfigHelper` type mismatch)
  - rerun4: `PRE01-03 PASS`, `STEP01-07 PASS`, `STEP08 FAIL` (`rc=1`, runner path mismatch)
  - rerun5 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, recovery retry executed once and failed with `rc=2`)

## Outcome Metrics
- Scoped declaration migrations retained: `26`.
- Scoped bounded rewires retained: `31` (`rewires-batchA-all.txt` + `rewires-batchB-all.txt`).
- Global tracked declarations/files remaining: `1057` (baseline `2277`, reduced `1220`).
- Hard-cut burndown completion: `53.579271%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `33.856805%`
  - Core total (01+02): `66.928403%`
  - Entire portfolio: `83.464201%`
- ETA refresh: ~`43.7h` (~`5.46` workdays).
