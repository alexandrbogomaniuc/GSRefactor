# Hard-Cut M2 Wave 204A/204B + Wave 205 Report

Date (UTC): 2026-02-27
Wave group: 204A + 204B + 205
Scope: declaration-first migration in `statistics.http`, `common.engine.tracker`, `common.promo.messages.server.notifications*`, `common.transactiondata.storeddate.identifier`, `bgm`, and `common.upload`.

## Batch Breakdown
- `W204A`: 10 declaration migrations in `statistics.http` and `common.engine.tracker`.
- `W204B`: 10 declaration migrations in promo notifications, stored-data identifiers, `bgm`, and upload callback/client surfaces.
- `W205`: integration and validation.

## Stabilization
- Primary mode executed as `1 explorer + 2 workers + main` with strict non-overlapping ownership.
- Zero-overlap selection validated up-front (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires retained only in direct importer/FQCN call-sites listed in `rewires-batchA-all.txt` and `rewires-batchB-all.txt`.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) out of migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-153111-hardcut-m2-wave204ab-wave205-parallel-batches/`
- Fast gate:
  - rerun1: `STEP01 FAIL` (`rc=1`, transient compile ordering)
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, recovery retry executed once and failed with `rc=2`)

## Outcome Metrics
- Scoped declaration migrations retained: `20`.
- Scoped bounded rewires retained: `60` (`rewires-batchA-all.txt` + `rewires-batchB-all.txt`).
- Global tracked declarations/files remaining: `1083` (baseline `2277`, reduced `1194`).
- Hard-cut burndown completion: `52.437418%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `33.135266%`
  - Core total (01+02): `66.567633%`
  - Entire portfolio: `83.283817%`
- ETA refresh: ~`44.8h` (~`5.60` workdays).
