# Hard-Cut M2 Wave 224A/224B + Wave 225 Report

Date (UTC): 2026-02-28
Wave group: 224A + 224B + 225
Scope: declaration-first migration in overlap-safe runtime surfaces with bounded rewires and canonical validation.

## Batch Breakdown
- `W224A`: 10 planned declaration migrations.
- `W224B`: 10 planned declaration migrations.
- `W225`: bounded rewires + integration validation.

## Stabilization
- Execution mode: `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned rewires: batchA `0`, batchB `10` (all in `mp-server/web` kafka handlers).
- Compile drift resolution sequence:
  - fast gate batchA rerun1/rerun2 failed at `STEP06` after moved declarations lost same-package visibility to legacy types.
  - applied minimal explicit compatibility imports in moved declarations (`KafkaOuterRequestHandler`, `KafkaInServiceRequestHandler`, `AbstractSendAlertException`, `KafkaRequest`, `BGPlayerDto`, `BotConfigInfoDto`).
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, prior uncommitted evidence folder `20260228-002035-*`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-010600-hardcut-m2-wave224ab-wave225-parallel-batches/`
- Fast gate batchA:
  - rerun1: `STEP06 FAIL`
  - rerun2: `STEP06 FAIL`
  - rerun3 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `20`.
- Bounded rewires retained: `10`.
- Global tracked declarations/files remaining: `861` (baseline `2277`, reduced `1416`).
- Hard-cut burndown completion: `62.186210%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `39.296100%`
  - Core total (01+02): `69.648050%`
  - Entire portfolio: `84.824025%`
- ETA refresh: ~`35.6h` (~`4.45` workdays).
