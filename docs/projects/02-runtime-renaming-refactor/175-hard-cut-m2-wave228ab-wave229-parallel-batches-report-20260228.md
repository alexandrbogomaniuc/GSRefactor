# Hard-Cut M2 Wave 228A/228B + Wave 229 Report

Date (UTC): 2026-02-28
Wave group: 228A + 228B + 229
Scope: declaration-first migration in overlap-safe runtime surfaces with canonical validation.

## Batch Breakdown
- `W228A`: 20 planned declaration migrations.
- `W228B`: 12 planned declaration migrations.
- `W229`: integration validation.

## Stabilization
- Execution mode: `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned rewires remained empty for both batches (`rewires-batchA-all.txt`, `rewires-batchB-all.txt`).
- Minimal in-file namespace alignment retained in owned `TBot.java` (`com.dgphoenix.casino.thrift.TBotState` -> `com.abs.casino.thrift.TBotState`).
- No additional compile stabilization required.
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, prior uncommitted evidence folder `20260228-002035-*`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-020557-hardcut-m2-wave228ab-wave229-parallel-batches/`
- Fast gate batchA:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `32`.
- Bounded rewires retained: `0`.
- Global tracked declarations/files remaining: `801` (baseline `2277`, reduced `1476`).
- Hard-cut burndown completion: `64.822135%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `40.961190%`
  - Core total (01+02): `70.480595%`
  - Entire portfolio: `85.240298%`
- ETA refresh: ~`33.2h` (~`4.15` workdays).
