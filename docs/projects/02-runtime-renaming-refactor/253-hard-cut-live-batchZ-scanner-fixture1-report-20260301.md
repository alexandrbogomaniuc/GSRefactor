# Project 02 Hard-Cut Live Batch Z Report (Scanner Fixture 1)

## Timestamp
- 2026-03-01 10:39 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `1` declaration/literal
- Retained declaration moves: `1`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-103939-hardcut-live-batchZ-scanner-fixture1`

## Retained Move (`com.dgphoenix -> com.abs`)
1. `phase9-abs-rename-candidate-scan-smoke.sh` fixture literal: `package com.dgphoenix.demo; -> package com.abs.demo;`

## Validation Evidence
- Focused fast-gate module summary:
  - `common`: `FAIL` (`rc=1`)
  - `common-wallet`: `FAIL` (`rc=1`)
  - `sb-utils`: `FAIL` (`rc=1`)
  - `common-gs`: `FAIL` (`rc=1`)
  - `common-promo`: `FAIL` (`rc=1`)
- Canonical runner (`run-rerun1.sh`) summary:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE01` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE01` (`mvn -DskipTests install`)
  - `step09_retry1`: `FAIL` (`rc=SKIP`)

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `1`
- Post-batch remaining: `0`
- Reduced total: `2277`
- Batch reduction: `1`
- Burndown: `100.000000%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `54.645725%`
- Core total (01+02): `77.322863%`
- Entire portfolio: `88.661431%`

## ETA Refresh
- Remaining declarations: `0`
- ETA: `~0.0h` (`~0.00` workdays)
