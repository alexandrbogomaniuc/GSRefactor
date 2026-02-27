# Hard-Cut M2 Wave 210A/210B + Wave 211 Report

Date (UTC): 2026-02-27
Wave group: 210A + 210B + 211
Scope: declaration-first migration in low-risk test surfaces (`sb-utils`, `utils`, `common-gs`, `common-wallet`, `common-promo`, `common-persisters`) with zero planned bounded rewires.

## Batch Breakdown
- `W210A`: 11 declaration migrations in `sb-utils`/`utils` test surfaces.
- `W210B`: 12 declaration migrations in `common-gs`/`common-wallet`/`common-promo`/`common-persisters` test surfaces.
- `W211`: integration and validation.

## Stabilization
- Primary mode executed as `1 explorer + 2 workers + main` with strict non-overlapping ownership.
- Zero-overlap selection validated up-front (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned bounded rewires remained empty (`rewires-batchA-all.txt` and `rewires-batchB-all.txt` both empty).
- Post-cut compile drift fixed with minimal test-only import/access adjustments in moved files:
  - restored explicit imports for still-unmigrated dependencies in migrated test packages,
  - adjusted one reflective constructor access path in `NtpTimeProviderTest`,
  - retained changes within owned batch files only.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) out of migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-170003-hardcut-m2-wave210ab-wave211-parallel-batches/`
- Fast gate:
  - rerun1: `STEP02 FAIL`
  - rerun2: `STEP02 FAIL`
  - rerun3: `STEP03/STEP04/STEP06 FAIL`
  - rerun4 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, retry executed once and failed with `rc=2`)

## Outcome Metrics
- Scoped declaration migrations retained: `23`.
- Scoped bounded rewires retained: `0` (planned rewire manifests empty; stabilization changes were in moved declaration files).
- Global tracked declarations/files remaining: `1006` (baseline `2277`, reduced `1271`).
- Hard-cut burndown completion: `55.819060%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `35.272132%`
  - Core total (01+02): `67.636066%`
  - Entire portfolio: `83.818033%`
- ETA refresh: ~`41.5h` (~`5.19` workdays).
