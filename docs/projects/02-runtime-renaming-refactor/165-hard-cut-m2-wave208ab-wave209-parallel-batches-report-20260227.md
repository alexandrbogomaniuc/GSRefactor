# Hard-Cut M2 Wave 208A/208B + Wave 209 Report

Date (UTC): 2026-02-27
Wave group: 208A + 208B + 209
Scope: declaration-first migration in `cbservtools` surfaces plus bounded battleground/bonus-lock/timeframe/payment-wallet interfaces.

## Batch Breakdown
- `W208A`: 18 declaration migrations in `gs.singlegames.tools.cbservtools` (`common-gs` + `sb-utils`).
- `W208B`: 10 declaration migrations in battleground/lock/wallet-client tests/timeframe/tournament handlers and bonus mass/restriction surfaces.
- `W209`: integration and validation.

## Stabilization
- Primary mode executed as `1 explorer + 2 workers + main` with strict non-overlapping ownership.
- Zero-overlap selection validated up-front (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires retained only in direct importer/FQCN call-sites listed in `rewires-batchA-all.txt` and `rewires-batchB-all.txt`.
- Fixed compile drift at `STEP06` by aligning `MassAwardBonusManager` imports to migrated restriction package (`com.abs...`).
- Fixed compile drift at `STEP07` by aligning battleground message imports in `BattlegroundControllerTest` and `MassAwardRestriction` JSP import in `support/getMassAwardRestrictions.jsp`.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) out of migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-163500-hardcut-m2-wave208ab-wave209-parallel-batches/`
- Fast gate:
  - rerun1: `STEP06 FAIL` (`rc=1`)
  - rerun2: `STEP07 FAIL` (`rc=1`)
  - rerun3: `STEP07 FAIL` (`rc=1`)
  - rerun4 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=1`, smoke alias)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=1`, recovery retry executed once and failed with `rc=1`)

## Outcome Metrics
- Scoped declaration migrations retained: `28`.
- Scoped bounded rewires retained: `43` (`rewires-batchA-all.txt` + `rewires-batchB-all.txt`).
- Global tracked declarations/files remaining: `1029` (baseline `2277`, reduced `1248`).
- Hard-cut burndown completion: `54.808959%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `34.633847%`
  - Core total (01+02): `67.316924%`
  - Entire portfolio: `83.658462%`
- ETA refresh: ~`42.5h` (~`5.32` workdays).
