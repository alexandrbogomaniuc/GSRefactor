# Hard-Cut M2 Wave 244A/244B + Wave 245 Report

Date (UTC): 2026-02-28
Wave group: 244A + 244B + 245
Scope: recover net declaration momentum via low-risk common-gs Kafka roundflow DTO package cuts with bounded compile stabilization.

## Batch Breakdown
- `W244A`: `10` declaration migrations retained (`common-gs` kafka add-win/payment DTO surfaces).
- `W244B`: `10` declaration migrations attempted in sit-in/sit-out DTO surfaces; `1` declaration reverted for collision (`SitOutRequest2`), retained `9`.
- `W245`: integration validation.

## Stabilization
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded fixes only (no blind/global replacement):
  - fast-gate runner alignment for `STEP06/STEP07` with `-Dcluster.properties=local/local-machine.properties`.
  - duplicate-FQCN compile drift recovery by reverting `SitOutRequest2` package migration in `common-gs` (kept mp-server `SitOutRequest2` in `com.abs`).
- Pre-existing unrelated local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, prior uncommitted evidence folders.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-044918-hardcut-m2-wave244ab-wave245-kafka-dto-roundflow/`
- Fast gate batchA:
  - rerun1: `STEP01-05 PASS`, `STEP06 FAIL` (`cluster.properties` unresolved)
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1: `STEP01-05 PASS`, `STEP06 FAIL` (duplicate FQCN drift)
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from prior checkpoint:
  - `com.dgphoenix -> com.abs`: `19`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+19`
- Global tracked declarations/files remaining: `669` (baseline `2277`, reduced `1608`).
- Hard-cut burndown completion: `70.619236%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `44.624391%`
  - Core total (01+02): `72.312195%`
  - Entire portfolio: `86.156098%`
- ETA refresh: ~`27.8h` (~`3.48` workdays).
