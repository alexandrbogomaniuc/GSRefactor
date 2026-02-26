# Hard-Cut M2 Wave 112A/112B + Wave 113 Report

Date (UTC): 2026-02-26
Wave group: 112A + 112B + 113
Scope: batched-safe parallel migration with strict no-overlap ownership.

## Batch breakdown
- `W112A`: migrated 12 start-game/login/processors declarations to `com.abs` with bounded request/form/action rewires.
- `W112B`: migrated 18 support/cache action declarations to `com.abs` with bounded rewires in `DomainNameAction` and `WEB-INF/struts-config.xml`.
- `W113`: integrated both batches and validated.

## Changed files
- Full file list: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-214021-hardcut-m2-wave112ab-wave113-parallel-batches/target-files.txt`

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-214021-hardcut-m2-wave112ab-wave113-parallel-batches/`
- Fast gate: PASS (`web-gs package`, `refactor smoke`)
- Full matrix: `9/9 PASS`

## Outcome metrics
- Scoped declaration migrations: `30`.
- Global tracked declarations/files remaining: `1904` (baseline `2277`, reduced `373`).
- Hard-cut burndown completion: `16.381203%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `27.047650%`
  - Core total (01+02): `63.523825%`
  - Entire portfolio: `81.761913%`
- ETA refresh: ~`88.2h` (~`11.03` workdays).
