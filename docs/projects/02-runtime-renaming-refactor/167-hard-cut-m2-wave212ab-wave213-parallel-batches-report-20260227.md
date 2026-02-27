# Hard-Cut M2 Wave 212A/212B + Wave 213 Report

Date (UTC): 2026-02-27
Wave group: 212A + 212B + 213
Scope: declaration-first migration across overlap-safe test/configuration surfaces with bounded rewires and per-batch fast gates.

## Batch Breakdown
- `W212A`: 10 declaration migrations in `common` test surfaces.
- `W212B`: 10 declaration migrations in `common`/`common-gs`/`rng` test surfaces and low-fanout `sb-utils` declarations.
- `W213`: bounded rewires + integration validation.

## Stabilization
- Primary mode executed as `1 explorer + 2 workers + main` with strict non-overlapping ownership.
- Overlap checks passed up-front (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned bounded rewires retained to 8 owned files only.
- Post-cut compile drift was resolved with minimal owned-file fixes:
  - restored explicit imports to still-unmigrated `com.dgphoenix` declarations in moved tests,
  - added protected-access helper in `AccountIdGeneratorTest` for `generateComposed(...)`,
  - fixed missing imports for moved test packages (`Pair`, `ForbiddenGamesForBonusProvider`, `ItemDistributor`, `RNG`).
- No blind/global replacement performed.
- Preserved unrelated local runtime edits (`cluster-hosts.properties`, `.tmp-w202-*`) outside commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-210136-hardcut-m2-wave212ab-wave213-parallel-batches/`
- Per-batch fast gates:
  - batchA rerun1: `STEP01 FAIL`
  - batchB rerun1: `STEP01 FAIL`
  - batchA rerun2: `STEP01 FAIL`
  - batchB rerun2: `STEP01 FAIL`
  - batchA rerun3: `STEP01 FAIL`
  - batchB rerun3: `STEP01 FAIL`
  - batchA rerun4: `STEP06 FAIL`
  - batchB rerun4: `STEP06 FAIL`
  - batchA rerun5 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias)
  - batchB rerun5 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias)
- Full matrix:
  - rerun1: `PRE01 FAIL`
  - rerun2: `PRE01-03 PASS`, `STEP01 FAIL`
  - rerun3: `PRE01-03 PASS`, `STEP01 FAIL`
  - rerun4: `PRE01-03 PASS`, `STEP01-05 PASS`, `STEP06 FAIL`
  - rerun5 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09` FAIL (`rc=2`)

## Outcome Metrics
- Scoped declaration migrations retained: `20`.
- Scoped bounded rewires retained: `8`.
- Global tracked declarations/files remaining: `986` (baseline `2277`, reduced `1291`).
- Hard-cut burndown completion: `56.697409%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `35.827162%`
  - Core total (01+02): `67.913581%`
  - Entire portfolio: `83.956791%`
- ETA refresh: ~`40.7h` (~`5.09` workdays).
