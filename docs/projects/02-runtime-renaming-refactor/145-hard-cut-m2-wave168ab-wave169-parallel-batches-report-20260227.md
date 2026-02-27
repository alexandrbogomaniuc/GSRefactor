# Hard-Cut M2 Wave 168A/168B + Wave 169 Report

Date (UTC): 2026-02-27
Wave group: 168A + 168B + 169
Scope: batched-safe migration with stabilization rollback on unsafe cross-boundary rewires.

## Batch Breakdown
- `W168A`: planned 5 declaration migrations with bounded rewires in `cassandra-cache/cache`, `common-persisters`, `common-gs`, and `promo/core`.
- `W168B`: planned 5 declaration migrations in `cassandra-cache/cache` + `persist/engine` with 1 bounded rewire.
- `W169`: integration stabilization.

## Stabilization
- Fast gate rerun1 failed at `common-persisters` install after broad Group A rewires crossed still-mixed namespace boundaries.
- Rolled back all Group A edits and high-risk Group B edits.
- Retained final scope:
  - `ICallInfo` declaration migration (`1`)
  - `NtpTimeGenerator` declaration migration (`1`)
  - total retained declarations/files: `2`

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-080044-hardcut-m2-wave168ab-wave169-parallel-batches/`
- Fast gate:
  - rerun1 FAIL at `common-persisters`
  - rerun2 PASS `9/9`
- Full matrix:
  - rerun1 FAIL due step08 path typo (`gs-server/mp-server/...`)
  - rerun2 PASS `9/9` with corrected step08 path `mp-server/persistance/pom.xml`

## Outcome Metrics
- Scoped declaration migrations retained: `2`.
- Scoped bounded rewires retained: `0`.
- Global tracked declarations/files remaining: `1298` (baseline `2277`, reduced `979`).
- Hard-cut burndown completion: `42.995169%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.374396%`
  - Core total (01+02): `65.187198%`
  - Entire portfolio: `82.593599%`
- ETA refresh: ~`53.5h` (~`6.69` workdays).
