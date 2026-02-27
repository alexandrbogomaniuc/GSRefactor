# Hard-Cut M2 Wave 152A/152B + Wave 153 Report

Date (UTC): 2026-02-27
Wave group: 152A + 152B + 153
Scope: batched-safe parallel migration with bounded integration stabilization.

## Batch Breakdown
- `W152A`: 20 declaration migrations in `sb-utils/common/util/web` + `utils/common/util/web`.
- `W152B`: 18 declaration migrations in `promo/persisters`.
- `W153`: bounded importer rewires across `common`, `common-wallet`, `common-gs`, `web-gs`, `promo/core`, `sb-utils`, and `utils`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-052810-hardcut-m2-wave152ab-wave153-parallel-batches/`
- Fast gate:
  - `rerun1` failed at `step8` (`common-gs`) due mixed canex request DTO lineage in `MQServiceHandler`.
  - bounded alignment applied in `MQServiceHandler` for canex request imports/FQCN status types to `com.abs` lineage.
  - `rerun2` PASS `10/10`.
- Full matrix:
  - `rerun1` PASS `9/9` with pre-setup installs (`utils`, `sb-utils`, `common-promo`, `promo-core`).

## Stabilization Notes
- Parallel execution mode:
  - explorer produced non-overlapping declaration batches with 3 rewire overlaps.
  - worker-thread cap limited concurrent workers; retained degraded-safe parallel mode (worker A + main-owned batch B), with strict file ownership maintained.
  - overlap rewires (`GameServer`, `MQServiceHandler`, `TournamentManager`) owned and merged by main agent only.
- Runtime-safety guardrail maintained:
  - no global replace; only package-scoped declaration migration and bounded rewires.

## Outcome Metrics
- Scoped declaration migrations retained: `38`.
- Scoped bounded rewires retained: `53`.
- Global tracked declarations/files remaining: `1442` (baseline `2277`, reduced `835`).
- Hard-cut burndown completion: `36.671058%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `29.583882%`
  - Core total (01+02): `64.791941%`
  - Entire portfolio: `82.395971%`
- ETA refresh: ~`59.3h` (~`7.42` workdays).
