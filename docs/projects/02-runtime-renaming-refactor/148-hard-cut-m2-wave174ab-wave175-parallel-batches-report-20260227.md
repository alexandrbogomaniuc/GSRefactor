# Hard-Cut M2 Wave 174A/174B + Wave 175 Report

Date (UTC): 2026-02-27
Wave group: 174A + 174B + 175
Scope: declaration-only cache-module migrations with no cross-module rewires.

## Batch Breakdown
- `W174A`: 2 declaration migrations in `cassandra`.
- `W174B`: 2 declaration migrations in `cassandra` + `persist/engine`.
- `W175`: integration and validation.

## Stabilization
- No rollback required.
- Fast-gate rerun1 failed only due smoke tooling script resolution (`validate-no-legacy-imports.sh` missing, exit 127).
- Reran fast gate with explicit smoke command; rerun2 passed.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-085908-hardcut-m2-wave174ab-wave175-parallel-batches/`
- Fast gate:
  - rerun1 FAIL at smoke tooling only
  - rerun2 PASS `9/9`
- Full matrix:
  - PASS `9/9` on rerun1

## Outcome Metrics
- Scoped declaration migrations retained: `4`.
- Scoped bounded rewires retained: `0`.
- Global tracked declarations/files remaining: `1288` (baseline `2277`, reduced `989`).
- Hard-cut burndown completion: `43.434343%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.429293%`
  - Core total (01+02): `65.214647%`
  - Entire portfolio: `82.607323%`
- ETA refresh: ~`53.1h` (~`6.64` workdays).
