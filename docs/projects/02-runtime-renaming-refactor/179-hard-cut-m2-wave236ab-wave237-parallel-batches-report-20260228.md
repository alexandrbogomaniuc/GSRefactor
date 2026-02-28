# Hard-Cut M2 Wave 236A/236B + Wave 237 Report

Date (UTC): 2026-02-28
Wave group: 236A + 236B + 237
Scope: declaration-first migration in overlap-safe runtime surfaces with canonical validation.

## Batch Breakdown
- `W236A`: 10 planned declaration migrations.
- `W236B`: 10 planned declaration migrations.
- `W237`: integration validation.

## Stabilization
- Execution remained declaration-first with strict ownership and overlap proofs.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Retained rewires:
  - Batch A: `0`
  - Batch B: `0`
- No additional compile stabilization was required.
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, prior uncommitted evidence folder `20260228-002035-*`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-031033-hardcut-m2-wave236ab-wave237-parallel-batches/`
- Fast gate batchA:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `20`.
- Bounded rewires retained: `0`.
- Global tracked declarations/files remaining: `719` (baseline `2277`, reduced `1558`).
- Hard-cut burndown completion: `68.423364%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `43.236813%`
  - Core total (01+02): `71.618407%`
  - Entire portfolio: `85.809203%`
- ETA refresh: ~`29.9h` (~`3.74` workdays).
