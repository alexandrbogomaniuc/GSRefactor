# Hard-Cut M2 Wave 230A/230B + Wave 231 Report

Date (UTC): 2026-02-28
Wave group: 230A + 230B + 231
Scope: declaration-first migration in overlap-safe runtime surfaces with canonical validation.

## Batch Breakdown
- `W230A`: 11 planned declaration migrations.
- `W230B`: 11 planned declaration migrations.
- `W231`: integration validation.

## Stabilization
- Execution mode target remained batched-safe with strict ownership and overlap proofs.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Retained rewires:
  - Batch A: `2` (in-module thrift class-level FQCN alignment in `TBGFriend`, `TBGOnlinePlayer`).
  - Batch B: `0` (external rewires discarded during stabilization).
- Compile drift handling:
  - transient `common-gs` compile drift emerged while testing optional batchB integration rewires.
  - resolved by dropping batchB external rewires and restoring declaration-first shape.
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, prior uncommitted evidence folder `20260228-002035-*`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-022003-hardcut-m2-wave230ab-wave231-parallel-batches/`
- Fast gate batchA:
  - rerun4 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun4 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `22`.
- Bounded rewires retained: `2`.
- Global tracked declarations/files remaining: `779` (baseline `2277`, reduced `1498`).
- Hard-cut burndown completion: `65.788318%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `41.571723%`
  - Core total (01+02): `70.785861%`
  - Entire portfolio: `85.392931%`
- ETA refresh: ~`32.3h` (~`4.04` workdays).
