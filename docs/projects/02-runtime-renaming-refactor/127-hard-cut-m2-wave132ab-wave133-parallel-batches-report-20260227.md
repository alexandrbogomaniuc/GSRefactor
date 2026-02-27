# Hard-Cut M2 Wave 132A/132B + Wave 133 Report

Date (UTC): 2026-02-27
Wave group: 132A + 132B + 133
Scope: batched-safe parallel migration with bounded integration rewires.

## Batch breakdown
- `W132A`: migrated 12 `cassandra-cache/common-persisters` `cassandra.persist.mp` declaration packages to `com.abs`.
- `W132B`: migrated 14 `sb-utils` `common.util.test.api` declaration packages to `com.abs`.
- `W133`: integrated both batches with bounded rewires in owned importer/test files.

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260227-011942-hardcut-m2-wave132ab-wave133-parallel-batches/`
- Fast gate: PASS on rerun1 (`sb-utils test`, `common-persisters install`, `common-gs install`, `web-gs package`, `refactor smoke`).
- Full matrix: `9/9 PASS`.

## Outcome metrics
- Scoped declaration migrations: `31` (including 5 aligned sb-utils test package declarations from owned rewires).
- Global tracked declarations/files remaining: `1688` (baseline `2277`, reduced `589`).
- Hard-cut burndown completion: `25.867369%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `28.233421%`
  - Core total (01+02): `64.116711%`
  - Entire portfolio: `82.058355%`
- ETA refresh: ~`70.0h` (~`8.75` workdays).
