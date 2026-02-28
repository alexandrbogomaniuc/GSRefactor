# Hard-Cut M2 Wave 226A/226B + Wave 227 Report

Date (UTC): 2026-02-28
Wave group: 226A + 226B + 227
Scope: declaration-first migration in overlap-safe runtime surfaces with bounded rewires and canonical validation.

## Batch Breakdown
- `W226A`: 14 planned declaration migrations.
- `W226B`: 14 planned declaration migrations.
- `W227`: bounded rewires + integration validation.

## Stabilization
- Execution mode: `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned rewires retained and bounded: batchA `1`, batchB `1` (both in `mp-server/thrift-api` services).
- Refined selection reduced initial high-rewire candidate to low-rewire execution plan (`total rewires=2`, no `web-gs` rewires).
- No additional compile stabilization required after worker edits.
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, prior uncommitted evidence folder `20260228-002035-*`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-014059-hardcut-m2-wave226ab-wave227-parallel-batches/`
- Fast gate batchA:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `28`.
- Bounded rewires retained: `2`.
- Global tracked declarations/files remaining: `833` (baseline `2277`, reduced `1444`).
- Hard-cut burndown completion: `63.416776%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `40.073142%`
  - Core total (01+02): `70.036571%`
  - Entire portfolio: `85.018286%`
- ETA refresh: ~`34.5h` (~`4.31` workdays).
