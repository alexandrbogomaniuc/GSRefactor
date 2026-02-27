# Hard-Cut M2 Wave 138A/138B + Wave 139 (Stabilized) Report

Date (UTC): 2026-02-27
Wave group: 138A + 138B + 139
Scope: batched-safe parallel migration with bounded integration rewires.

## Batch Breakdown
- Planned `W138A`: 15 declarations in `common-gs/kafka/dto/privateroom/{request,response}`.
- Planned `W138B`: 12 declarations in `sb-utils/common/vault` and `sb-utils/common/util/xml/xstreampool`.
- Integration `W139`: bounded rewire updates in owned importer files.

## Stabilization Outcome
- Retained:
  - `W138A` declarations (`15`)
  - bounded common-gs rewires (`3`)
- Deferred:
  - `W138B` declarations and its related rewire due compatibility safety.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-030611-hardcut-m2-wave138ab-wave139-parallel-batches/`
- `rerun1`:
  - fast gate FAIL at `step3`
  - full matrix FAIL at `step05`
- `rerun2` (stabilized):
  - fast gate PASS `5/5`
  - full matrix PASS `9/9`

## Outcome Metrics
- Global tracked declarations/files remaining: `1627` (baseline `2277`, reduced `650`).
- Hard-cut burndown completion: `28.546333%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `28.568292%`
  - Core total (01+02): `64.284146%`
  - Entire portfolio: `82.142073%`
- ETA refresh: ~`67.1h` (~`8.39` workdays).
