# Hard-Cut M2 Wave 170A/170B + Wave 171 Report

Date (UTC): 2026-02-27
Wave group: 170A + 170B + 171
Scope: batched-safe migration with stabilization rollback on unsafe cross-boundary rewires.

## Batch Breakdown
- `W170A`: planned 2 declaration migrations with bounded rewires in `common-persisters`, `common-gs`, and `web-gs`.
- `W170B`: planned 2 declaration migrations with bounded rewires in `cache`, `common-gs`, `mp-server`, and `web-gs`.
- `W171`: integration stabilization.

## Stabilization
- Fast gate rerun1 failed at `common-persisters` compile due unresolved `com.abs.casino.cassandra.IEntityUpdateListener` after broad rewires.
- Rolled back all initial A/B edits.
- Retained final scope:
  - `ColumnIteratorCallback` declaration migration (`1`)
  - `FakeNotAppliedResultSet` declaration migration (`1`)
  - `AbstractCassandraPersister` bounded import rewire (`1`)
  - total retained declarations/files: `2` (+ `1` bounded rewire)

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-082639-hardcut-m2-wave170ab-wave171-parallel-batches/`
- Fast gate:
  - rerun1 FAIL at `common-persisters`
  - rerun2 PASS `9/9`
- Full matrix:
  - rerun1 PASS `9/9`

## Outcome Metrics
- Scoped declaration migrations retained: `2`.
- Scoped bounded rewires retained: `1`.
- Global tracked declarations/files remaining: `1296` (baseline `2277`, reduced `981`).
- Hard-cut burndown completion: `43.083004%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.385376%`
  - Core total (01+02): `65.192688%`
  - Entire portfolio: `82.596344%`
- ETA refresh: ~`53.4h` (~`6.68` workdays).
