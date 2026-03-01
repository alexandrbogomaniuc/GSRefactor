# Project 02 Hard-Cut Live Batch I Report (Cassandra/Promo Interfaces)

## Timestamp
- 2026-03-01 09:12 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `8`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-091238-hardcut-live-batchI-cassandra-promo-interfaces10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `ICachePersister`
2. `ICassandraBaseGameInfoPersister`
3. `IHttpClientStatisticsPersister`
4. `ILazyLoadingPersister`
5. `ExtendedAccountInfoPersisterInstanceHolder`
6. `IStringSerializer`
7. `IPromoCountryRestrictionService`
8. `INetworkPromoCampaign`

## Deferred During Stabilization
- `IRemotePromoNotifier`
- `ILoadBalancer`

Reason:
- deferred to avoid pulling pre-existing modified `common-gs`/cross-module boundary surfaces into this push; retained strict low-risk interface-first scope.

## Bounded Compatibility Rewires
- Updated callsite imports to moved cassandra interfaces/holder in clean files (`RESTCWClient`, `HttpClientCallbackHandler`, `CurrencyCache`, `BaseGameCache`, `AbstractLazyLoadingExportableCache`).
- Updated promo service wiring imports for moved promo interfaces (`CountryRestrictionService`, `GameServerComponentsConfiguration`, `NetworkPromoCampaign`, `IPromoCampaignManager`, `PromoCampaignManager`).

## Validation Evidence
- Canonical runner (`run-rerun1.sh`) on retained state:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `step09_retry1`: `SKIP`
- Observed failure profile remains consistent with existing dirty workspace blockers:
  - `common` duplicate-class drift (`CacheExportProcessor`),
  - `common-promo` drift profile,
  - downstream `common-gs` cassandra import drift.

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `198`
- Post-batch remaining: `190`
- Reduced total: `2087`
- Batch reduction: `8`
- Burndown: `91.655687%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `51.957593%`
- Core total (01+02): `75.978797%`
- Entire portfolio: `87.989398%`

## ETA Refresh
- Remaining declarations: `190`
- ETA: `~7.7h` (`~0.96` workdays)
