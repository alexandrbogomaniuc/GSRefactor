# Hard-Cut M2 Wave 128A/128B + Wave 129 Report

Date (UTC): 2026-02-27
Wave group: 128A + 128B + 129
Scope: batched-safe parallel migration with bounded integration rewires.

## Batch breakdown
- `W128A`: migrated 11 `support/archiver` declaration packages to `com.abs` and rewired runtime launcher scripts.
- `W128B`: migrated 14 `common-promo/feed/tournament` declaration packages to `com.abs` and rewired bounded importers.
- `W129`: integrated both batches and stabilized dependency-order validation for touched promo/common-gs build path.

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260227-003110-hardcut-m2-wave128ab-wave129-parallel-batches/`
- Fast gate:
  - initial reruns exposed dependency-order/type drift while mixed local artifacts were in play.
  - bounded stabilization applied (`common-promo` + `promo/persisters` pre-install).
  - rerun4 passed (`5/5`): `common-promo install`, `promo/persisters install`, `common-gs install`, `web-gs package`, `refactor smoke`.
- Full matrix: `9/9 PASS`.

## Outcome metrics
- Scoped declaration migrations: `25`.
- Global tracked declarations/files remaining: `1747` (baseline `2277`, reduced `530`).
- Hard-cut burndown completion: `23.276241%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `27.909530%`
  - Core total (01+02): `63.954765%`
  - Entire portfolio: `81.977383%`
- ETA refresh: ~`72.5h` (~`9.06` workdays).
