# Project 02 Hard-Cut Live Batch Y Report (Residual Java 12)

## Timestamp
- 2026-03-01 10:37 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `12` declarations
- Retained declaration moves: `12`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-103720-hardcut-live-batchY-residual-java12`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `CassandraPersistenceManager`
2. `AbstractCassandraPersister`
3. `TableDefinition`
4. `DesiredPrize`
5. `InstantMoneyPrize`
6. `NetworkPromoEvent`
7. `TicketPrize`
8. `TimeSlot`
9. `TournamentPrize`
10. `TournamentRankQualifier`
11. `GameServer`
12. `NtpTimeProvider`

## Bounded Compatibility Rewires
- Package migration for residual Java declarations in previously dirty files.
- Bounded import rewires: switched `import com.dgphoenix...` to `import com.abs...` only when imported source declaration is already moved.
- No blind/global replace.

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

## Residual Legacy Package Blocker
- Post-scan shows one remaining `package com.dgphoenix` occurrence in:
  - `gs-server/deploy/scripts/phase9-abs-rename-candidate-scan-smoke.sh`
- This is a scanner smoke fixture text literal, not runtime Java production code.

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `13`
- Post-batch remaining: `1`
- Reduced total: `2276`
- Batch reduction: `12`
- Burndown: `99.956083%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `54.632217%`
- Core total (01+02): `77.316109%`
- Entire portfolio: `88.658054%`

## ETA Refresh
- Remaining declarations: `1`
- ETA: `~0.05h` (`~0.01` workdays)
