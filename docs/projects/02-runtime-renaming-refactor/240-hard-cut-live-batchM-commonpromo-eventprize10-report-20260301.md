# Project 02 Hard-Cut Live Batch M Report (Common-Promo Event/Prize Cluster)

## Timestamp
- 2026-03-01 09:48 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `10`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-094846-hardcut-live-batchM-commonpromo-eventprize10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `NetworkTournamentPromoTemplate`
2. `CacheBonusPrize`
3. `FRBonusPrize`
4. `BetAmountPrizeQualifier`
5. `EndRoundEvent`
6. `RoundStat`
7. `TournamentMemberRanks`
8. `RankRange`
9. `PlayerBonusEvent`
10. `RoundQualificationStat`

## Bounded Compatibility Rewires
- Added explicit legacy imports inside moved declarations for unmoved promo dependencies (`AbstractPrize`, `AbstractParticipantEvent`, `SignificantEventType`, `IPrizeQualifier`, `IPromoTemplate`, `PromoCampaignMember`, `DesiredPrize`, `TournamentObjective`, `TournamentPromoTemplate`, `TournamentRankQualifier`, `TournamentMemberRank`).
- Rewired direct explicit imports for moved `TournamentMemberRanks` in:
  - `TournamentRanksExtractor`
  - `CassandraTournamentRankPersister`
  - `PromoTournamentRankChangesProcessor`
- No blind/global replace.

## Validation Evidence
- Focused fast-gate module summary:
  - `common-promo`: `FAIL` (`rc=1`, existing mixed promo/util drift profile)
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
- Pre-batch remaining: `159`
- Post-batch remaining: `149`
- Reduced total: `2128`
- Batch reduction: `10`
- Burndown: `93.456302%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `52.511429%`
- Core total (01+02): `76.255715%`
- Entire portfolio: `88.127857%`

## ETA Refresh
- Remaining declarations: `149`
- ETA: `~6.1h` (`~0.76` workdays)
