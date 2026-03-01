# Project 02 Hard-Cut Live Batch K Report (Promo + Cache/Util/Cassandra Interfaces)

## Timestamp
- 2026-03-01 09:32 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `10`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-093214-hardcut-live-batchK-promo-cache-util-cassandra10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `KeyspaceManagerStatistics`
2. `IHighFrequencyPrize`
3. `IMoneyPrize`
4. `INetworkPromoEvent`
5. `IRemotePromoNotifier`
6. `ITournamentRankQualifier`
7. `IVirtualPrize`
8. `ExportableCacheEntryContainer`
9. `ITimeProvider`
10. `ILoadBalancer`

## Bounded Compatibility Rewires
- `AbstractLockManager` and `LoadBalancerCache` imports rewired to moved `com.abs.casino.common.ILoadBalancer`.
- `ParticipantEventProcessor` and `RemoteCallHelper` explicitly rewired to moved `com.abs.casino.common.promo.IRemotePromoNotifier`.
- No blind/global replace; rewires were limited to direct consumers.

## Validation Evidence
- Focused fast-gate module summary:
  - `common-promo`: `FAIL` (`rc=1`, existing mixed promo/util drift and duplicate-FQCN profile)
  - `sb-utils`: `PASS` (`rc=0`)
  - `cassandra-cache/cache`: `FAIL` (`rc=1`, pre-existing cache engine drift)
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
- Pre-batch remaining: `179`
- Post-batch remaining: `169`
- Reduced total: `2108`
- Batch reduction: `10`
- Burndown: `92.577953%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `52.241265%`
- Core total (01+02): `76.120633%`
- Entire portfolio: `88.060316%`

## ETA Refresh
- Remaining declarations: `169`
- ETA: `~6.9h` (`~0.86` workdays)
