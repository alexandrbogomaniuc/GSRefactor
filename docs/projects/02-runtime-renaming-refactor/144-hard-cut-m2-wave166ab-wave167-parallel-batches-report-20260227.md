# Hard-Cut M2 Wave 166A/166B + Wave 167 Report

Date (UTC): 2026-02-27
Wave group: 166A + 166B + 167
Scope: batched-safe migration with stabilization rollback on unsafe cross-boundary rewires.

## Batch Breakdown
- `W166A`: 10 declaration migrations in `cassandra-cache/cache` keyspace/configuration scope.
- `W166B`: planned 10 declaration migrations in `cassandra-cache/cache` factory/locking/persist scope with bounded cross-module rewires.
- `W167`: integration stabilization.

## Stabilization
- Fast gate rerun1 failed at `common-persisters` with unresolved-symbol cascade in `CassandraTransactionDataPersister` after Batch B + broad rewires crossed still-mixed namespace boundaries.
- Rolled back all main-owned Batch B and overlap rewires.
- Retained final scope:
  - Batch A declarations: `10`
  - Shared declaration retained: `PersistersFactory` (`1`)
  - Additional bounded rewire retained: `IKeyspaceManager` (`1`)
  - total retained declarations/files: `12`

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-073734-hardcut-m2-wave166ab-wave167-parallel-batches/`
- Fast gate:
  - final PASS `9/9` on rerun2.
- Full matrix:
  - PASS `9/9` on rerun1.

## Outcome Metrics
- Scoped declaration migrations retained: `12`.
- Scoped bounded rewires retained: `1`.
- Global tracked declarations/files remaining: `1300` (baseline `2277`, reduced `977`).
- Hard-cut burndown completion: `42.907334%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.363417%`
  - Core total (01+02): `65.181709%`
  - Entire portfolio: `82.590854%`
- ETA refresh: ~`53.6h` (~`6.70` workdays).
