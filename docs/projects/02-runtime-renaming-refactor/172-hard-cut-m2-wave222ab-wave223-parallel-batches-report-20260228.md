# Hard-Cut M2 Wave 222A/222B + Wave 223 Report

Date (UTC): 2026-02-28
Wave group: 222A + 222B + 223
Scope: declaration-first migration in overlap-safe runtime surfaces with bounded rewires and canonical validation.

## Batch Breakdown
- `W222A`: 12 planned declaration migrations.
- `W222B`: 12 planned declaration migrations.
- `W223`: bounded rewires + integration validation.

## Stabilization
- Execution mode: `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Planned rewires remained empty for both batches (`rewires-batchA-all.txt`, `rewires-batchB-all.txt`).
- Compile drift resolution sequence:
  - `STEP04`: moved `common-promo` declarations lost same-package visibility to legacy promo declarations still in `com.dgphoenix`.
  - applied minimal explicit import in moved promo declarations: `import com.dgphoenix.casino.common.promo.*;`.
  - proactively applied same-package compatibility import used in prior handler waves: `import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;` in moved handlers.
- No blind/global replacement performed.
- Pre-existing local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, earlier uncommitted evidence folder `20260228-002035-*`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-004400-hardcut-m2-wave222ab-wave223-parallel-batches/`
- Fast gate batchA:
  - rerun1: `STEP04 FAIL`
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Planned declaration migrations retained: `24`.
- Bounded rewires retained: `0`.
- Global tracked declarations/files remaining: `881` (baseline `2277`, reduced `1396`).
- Hard-cut burndown completion: `61.308740%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `38.741070%`
  - Core total (01+02): `69.370535%`
  - Entire portfolio: `84.685268%`
- ETA refresh: ~`36.4h` (~`4.55` workdays).
