# Hard-Cut M2 Wave 134A/134B + Wave 135 Report

Date (UTC): 2026-02-27
Wave group: 134A + 134B + 135
Scope: batched-safe parallel migration with bounded integration rewires.

## Batch breakdown
- `W134A`: migrated 10 `sb-utils/common.util.xml.parser` declaration packages to `com.abs`.
- `W134B`: migrated 10 `promo.events.process` declaration packages to `com.abs`.
- `W135`: integrated both batches and applied bounded compatibility alignment in `HistoryInformerManager` (`HistoryInformerItem` package type match).

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260227-013115-hardcut-m2-wave134ab-wave135-parallel-batches/`
- Fast gate: PASS on rerun4 (`sb-utils test`, `promo-core install`, `common-gs install`, `web-gs package`, `refactor smoke`).
- Full matrix: `9/9 PASS` on rerun4.

## Outcome metrics
- Scoped declaration migrations: `20`.
- Global tracked declarations/files remaining: `1668` (baseline `2277`, reduced `609`).
- Hard-cut burndown completion: `26.745718%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `28.343215%`
  - Core total (01+02): `64.171607%`
  - Entire portfolio: `82.085804%`
- ETA refresh: ~`68.8h` (~`8.60` workdays).
