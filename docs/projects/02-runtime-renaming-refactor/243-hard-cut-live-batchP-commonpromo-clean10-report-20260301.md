# Project 02 Hard-Cut Live Batch P Report (Common-Promo Clean 10)

## Timestamp
- 2026-03-01 10:02 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `10`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-100156-hardcut-live-batchP-commonpromo-clean10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `PrizeStatus`
2. `TournamentPromoTemplate`
3. `AwardedPrize`
4. `PromoNotificationType`
5. `IPrize`
6. `MaxBalanceTournamentPlayerDetails`
7. `IPromoCampaignManager`
8. `TournamentObjective`
9. `SignificantEventType`
10. `TournamentMemberRank`

## Bounded Compatibility Rewires
- Package-only declaration migration for retained clean targets.
- No blind/global replace.
- No high-fanout manager/core implementation movement in this batch.

## Validation Evidence
- Focused fast-gate module summary:
  - `common-promo`: `FAIL` (`rc=1`, existing mixed promo/util + duplicate-class drift profile)
  - `promo-core`: `FAIL` (`rc=1`, mixed promo symbol drift)
  - `promo-persisters`: `FAIL` (`rc=1`, pre-existing cassandra engine package drift)
  - `common-gs`: `FAIL` (`rc=1`, pre-existing mixed import drift)
  - `common`: `FAIL` (`rc=1`, known duplicate-class drift profile)
- Canonical runner (`run-rerun1.sh`) summary:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `step09_retry1`: `SKIP`

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `129`
- Post-batch remaining: `119`
- Reduced total: `2158`
- Batch reduction: `10`
- Burndown: `94.773825%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `52.916675%`
- Core total (01+02): `76.458337%`
- Entire portfolio: `88.229169%`

## ETA Refresh
- Remaining declarations: `119`
- ETA: `~4.9h` (`~0.61` workdays)
