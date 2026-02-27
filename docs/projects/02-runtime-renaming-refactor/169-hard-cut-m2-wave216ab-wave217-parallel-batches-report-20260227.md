# Hard-Cut M2 Wave 216A/216B + Wave 217 Report

Date (UTC): 2026-02-27
Wave group: 216A + 216B + 217
Scope: declaration-first migration in overlap-safe runtime surfaces with bounded rewires and canonical validation.

## Batch Breakdown
- `W216A`: 10 planned declaration migrations.
- `W216B`: 10 planned declaration migrations.
- `W217`: bounded rewires + integration validation.

## Stabilization
- Execution mode: `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned bounded rewires retained (`6 + 6` files) and expanded only where compile stabilization required.
- Compile drift resolution sequence:
  - `STEP03`: resolved same-package coupling after package cut (`IGameLogger`, `BufferedXmlWriter`, `LockInfo`, `RawStatisticsServlet`) via explicit legacy-type imports in owning/non-overlapping files.
  - `STEP06`: resolved chained `common-gs` type/import drift from moved symbols (`IGameServer`, `ILasthandPersister`, `NoneBetPersister`, `KafkaMessageService`) with minimal explicit imports and one stabilization declaration move of `KafkaMessageService` to `com.abs`.
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-230214-hardcut-m2-wave216ab-wave217-parallel-batches/`
- Fast gate batchA:
  - rerun1: `STEP03 FAIL`
  - rerun2: `STEP03 FAIL`
  - rerun3..9: `STEP06 FAIL` (iterative stabilization)
  - rerun10 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `20`.
- Additional stabilization declaration migration retained: `1` (`KafkaMessageService`).
- Total declaration migrations retained this wave: `21`.
- Bounded rewires retained (planned + stabilization rewires): `20`.
- Global tracked declarations/files remaining: `945` (baseline `2277`, reduced `1332`).
- Hard-cut burndown completion: `58.498024%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `36.964974%`
  - Core total (01+02): `68.482487%`
  - Entire portfolio: `84.241243%`
- ETA refresh: ~`39.0h` (~`4.88` workdays).
