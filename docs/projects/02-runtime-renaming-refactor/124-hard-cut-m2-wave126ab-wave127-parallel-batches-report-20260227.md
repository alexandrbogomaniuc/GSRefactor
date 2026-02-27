# Hard-Cut M2 Wave 126A/126B + Wave 127 Report

Date (UTC): 2026-02-27
Wave group: 126A + 126B + 127
Scope: batched-safe parallel migration with bounded integration rewires.

## Batch breakdown
- `W126A`: migrated 16 `common-gs` inservice Kafka handler declaration packages to `com.abs`.
- `W126B`: migrated 12 `common-gs` API XML request/response declaration packages to `com.abs`.
- `W127`: integrated both batches and stabilized a bounded JSP import/type mismatch.

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260227-001501-hardcut-m2-wave126ab-wave127-parallel-batches/`
- Fast gate:
  - rerun1 failed in `web-gs package` due `tools/api/service.jsp` import/type drift.
  - rerun2 passed (`common-gs install`, `web-gs package`, `refactor smoke`).
- Full matrix: `9/9 PASS`.

## Outcome metrics
- Scoped declaration migrations: `28`.
- Global tracked declarations/files remaining: `1772` (baseline `2277`, reduced `505`).
- Hard-cut burndown completion: `22.178305%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `27.772288%`
  - Core total (01+02): `63.886144%`
  - Entire portfolio: `81.943072%`
- ETA refresh: ~`76.5h` (~`9.56` workdays).
