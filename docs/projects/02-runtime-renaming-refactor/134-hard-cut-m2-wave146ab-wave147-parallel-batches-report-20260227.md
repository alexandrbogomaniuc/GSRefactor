# Hard-Cut M2 Wave 146A/146B + Wave 147 Report

Date (UTC): 2026-02-27
Wave group: 146A + 146B + 147
Scope: batched-safe parallel migration with bounded integration stabilization.

## Batch Breakdown
- `W146A`: 20 declaration migrations in `sb-utils/common/mp`.
- `W146B`: 18 declaration migrations in `sb-utils/common/util/xml` plus `common-gs` xml parser test package.
- `W147`: bounded importer rewires and one compatibility bridge fix in `common-wallet`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-043019-hardcut-m2-wave146ab-wave147-parallel-batches/`
- Fast gate:
  - rerun1 failed (`common-gs` compile with stale dependency graph)
  - rerun2 failed (`common-gs` mixed `MQData` type identity)
  - rerun3 PASS `5/5`
- Full matrix:
  - rerun1 failed at `step02` (`common-wallet` latent package bridge issue surfaced)
  - rerun2 PASS `9/9`

## Stabilization Notes
- Dependency-order alignment retained:
  - `common-persisters` install now precedes `common-gs` validation for this wave.
- Bounded compatibility fix retained:
  - `CanexCWClient` now explicitly extends `com.abs.casino.payment.wallet.client.v4.RESTCWClient` to preserve `com.dgphoenix` call sites while compiling against migrated v4 client classes.
- Runtime-safety guardrail maintained:
  - no global replace; only package-scoped declaration migration and bounded import rewires.

## Outcome Metrics
- Scoped declaration migrations retained: `38`.
- Scoped bounded rewires retained: `36`.
- Global tracked declarations/files remaining: `1521` (baseline `2277`, reduced `756`).
- Hard-cut burndown completion: `33.201581%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `29.150198%`
  - Core total (01+02): `64.575099%`
  - Entire portfolio: `82.287549%`
- ETA refresh: ~`62.6h` (~`7.83` workdays).
