# Hard-Cut M2 Wave 140A/140B + Wave 141 (Stabilized) Report

Date (UTC): 2026-02-27
Wave group: 140A + 140B + 141
Scope: batched-safe parallel migration with bounded stabilization.

## Batch Breakdown
- Planned `W140A`: 14 declarations in `common-promo/messages/{client/requests,server/notifications/prizes,server/responses}`.
- Planned `W140B`: 14 declarations in `sb-utils/src/test` utility tests.
- Integration `W141`: bounded validation/integration stabilization.

## Stabilization Outcome
- Retained:
  - `W140A` declarations (`14`)
- Deferred:
  - `W140B` declarations (`14`) due package-compatibility drift in `sb-utils` test scope under current runtime namespace state.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-032629-hardcut-m2-wave140ab-wave141-parallel-batches/`
- `rerun1`:
  - fast gate FAIL at `step1` (`sb-utils` test compile)
- `rerun2`:
  - fast gate FAIL at `step3` (`common-gs` tests not skipped; arm64 LZ4 native mismatch)
- `rerun3` (stabilized):
  - fast gate PASS `5/5`
  - full matrix PASS `9/9`

## Outcome Metrics
- Global tracked declarations/files remaining: `1613` (baseline `2277`, reduced `664`).
- Hard-cut burndown completion: `29.161177%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `28.645147%`
  - Core total (01+02): `64.322574%`
  - Entire portfolio: `82.161287%`
- ETA refresh: ~`66.5h` (~`8.31` workdays).
