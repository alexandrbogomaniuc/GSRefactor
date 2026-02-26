# Hard-Cut M2 Wave 122A/122B + Wave 123 Report

Date (UTC): 2026-02-26
Wave group: 122A + 122B + 123
Scope: batched-safe parallel migration with bounded integration rewires.

## Batch breakdown
- `W122A`: migrated 12 `common-gs` DTO/message/service declaration packages to `com.abs`.
- `W122B`: migrated 10 `common-gs` configuration/initializer declaration packages to `com.abs`.
- `W123`: integrated both batches in `web-gs` rewires and stabilized battleground/Kafka type compatibility.

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-231233-hardcut-m2-wave122ab-wave123-parallel-batches/`
- Fast gate:
  - initial and multiple reruns during compatibility alignment.
  - final rerun10 passed (`common-gs install`, `web-gs package`, `refactor smoke`).
- Full matrix: `9/9 PASS`.

## Outcome metrics
- Scoped declaration migrations: `22`.
- Global tracked declarations/files remaining: `1821` (baseline `2277`, reduced `456`).
- Hard-cut burndown completion: `20.026350%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `27.503294%`
  - Core total (01+02): `63.751647%`
  - Entire portfolio: `81.875823%`
- ETA refresh: ~`84.5h` (~`10.56` workdays).
