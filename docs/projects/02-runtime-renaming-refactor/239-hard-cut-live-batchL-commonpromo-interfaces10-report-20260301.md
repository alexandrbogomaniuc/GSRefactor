# Project 02 Hard-Cut Live Batch L Report (Common-Promo Interfaces + DTO)

## Timestamp
- 2026-03-01 09:41 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `10`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-094152-hardcut-live-batchL-commonpromo-interfaces10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `ITournamentPromoTemplate`
2. `IConcurrentPromoTemplate`
3. `IPrizeWonHelper`
4. `IPromoCampaignsObserver`
5. `IPrizeWonHandler`
6. `INetworkPromoEventTemplate`
7. `ICampaignStatisticsProvider`
8. `TournamentPlayerDetails`
9. `GameBonusKey`
10. `SupportedPlatform`

## Bounded Compatibility Rewires
- Added explicit legacy imports in moved interfaces for unmoved promo types (`DesiredPrize`, `IPrize`, `IPromoTemplate`, `Status`, `PromoCampaignMember`, `TournamentObjective`, `TournamentRankQualifier`).
- Rewired direct consumer imports only:
  - `INetworkPromoEvent`, `NetworkTournamentEvent`, `MaxPerformanceTournamentTest`, `PrizeWonBalanceChanger`, `NotAvailableStatisticsProvider`, `CassandraSupportedPromoPlatformsPersister`.
- No blind/global replace.

## Validation Evidence
- Focused fast-gate module summary:
  - `common-promo`: `FAIL` (`rc=1`, known mixed promo/util drift profile)
  - `promo-core`: `FAIL` (`rc=1`, moved-type symbol drift in current mixed workspace)
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
- Pre-batch remaining: `169`
- Post-batch remaining: `159`
- Reduced total: `2118`
- Batch reduction: `10`
- Burndown: `93.017127%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `52.376347%`
- Core total (01+02): `76.188174%`
- Entire portfolio: `88.094087%`

## ETA Refresh
- Remaining declarations: `159`
- ETA: `~6.5h` (`~0.81` workdays)
