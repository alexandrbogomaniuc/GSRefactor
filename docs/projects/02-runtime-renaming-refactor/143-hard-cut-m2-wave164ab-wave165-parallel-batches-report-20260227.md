# Hard-Cut M2 Wave 164A/164B + Wave 165 Report

Date (UTC): 2026-02-27
Wave group: 164A + 164B + 165
Scope: batched-safe migration with stabilization rollback on unsafe batch.

## Batch Breakdown
- `W164A`: 12 declaration migrations in `cassandra` test scopes plus 1 bounded rewire in `cache/src/test/resources/ClusterConfig.xml`.
- `W164B`: planned 12 declaration migrations in `sb-utils` test scopes.
- `W165`: integration stabilization.

## Stabilization
- Fast gate rerun1 failed at `sb-utils` install with broad `testCompile` symbol-resolution failures after Batch B package migration.
- Batch B was rolled back to `HEAD` to preserve runtime/build safety and wave momentum.
- Retained final migration scope:
  - declarations: `12` (Batch A only)
  - rewires: `1`

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-071748-hardcut-m2-wave164ab-wave165-parallel-batches/`
- Fast gate:
  - final PASS `9/9` on rerun2.
- Full matrix:
  - PASS `9/9` on rerun1.

## Outcome Metrics
- Scoped declaration migrations retained: `12`.
- Scoped bounded rewires retained: `1`.
- Global tracked declarations/files remaining: `1312` (baseline `2277`, reduced `965`).
- Hard-cut burndown completion: `42.380325%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.297541%`
  - Core total (01+02): `65.148771%`
  - Entire portfolio: `82.574385%`
- ETA refresh: ~`54.1h` (~`6.76` workdays).
