# Hard-Cut M2 Wave 218A/218B + Wave 219 Report

Date (UTC): 2026-02-27
Wave group: 218A + 218B + 219
Scope: declaration-first migration in overlap-safe runtime surfaces with bounded rewires and canonical validation.

## Batch Breakdown
- `W218A`: 10 planned declaration migrations.
- `W218B`: 10 planned declaration migrations.
- `W219`: bounded rewires + integration validation.

## Stabilization
- Execution mode: `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned rewires remained empty for both batches (`rewires-batchA-all.txt`, `rewires-batchB-all.txt`).
- Compile drift resolution sequence:
  - `STEP01`: aligned imports after declaration cut for moved common-cache/game declarations (`IDistributedConfigCache`, `ICreateGameListener`, `MiniGameInfo`, `GameLanguageHelper`, `RoundFinishedHelper`).
  - `STEP03`: aligned `sb-utils` JSON interface lineage by explicitly importing moved `com.abs` declarations in legacy-package interfaces (`JsonSelfSerializable`, `JsonDeserializableDeserializer`, `JsonDeserializableModule`).
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-233414-hardcut-m2-wave218ab-wave219-parallel-batches/`
- Fast gate batchA:
  - rerun1: `STEP01 FAIL`
  - rerun2: `STEP01 FAIL`
  - rerun3: `STEP03 FAIL`
  - rerun4: non-canonical path drift (`STEP04` wrong module path), discarded
  - rerun5 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: non-canonical PRE path drift (`PRE02` wrong module path), discarded
  - rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `20`.
- Bounded rewires retained: `0`.
- Global tracked declarations/files remaining: `925` (baseline `2277`, reduced `1352`).
- Hard-cut burndown completion: `59.376373%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `37.520004%`
  - Core total (01+02): `68.760002%`
  - Entire portfolio: `84.380001%`
- ETA refresh: ~`38.2h` (~`4.78` workdays).
