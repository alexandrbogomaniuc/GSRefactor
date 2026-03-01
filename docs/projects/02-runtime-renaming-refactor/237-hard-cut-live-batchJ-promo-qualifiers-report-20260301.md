# Project 02 Hard-Cut Live Batch J Report (Promo Qualifiers/Interfaces)

## Timestamp
- 2026-03-01 09:23 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `15` declarations
- Retained declaration moves: `11`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-092323-hardcut-live-batchJ-promo-qualifiers15`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `AlwaysQualifyBetQualifier`
2. `ByAmountBetEventQualifier`
3. `NoPrizeQualifier`
4. `SpinCountPrizeQualifier`
5. `DelegatedEventQualifier`
6. `IPlayerBetQualifier`
7. `IPlayerBonusQualifier`
8. `IPlayerWinQualifier`
9. `TournamentSimpleBetEventQualifier`
10. `ISupportedPlatform`
11. `IPrizeWonHandlersFactory`

## Not Applicable (Already Migrated In HEAD)
- `WinQualifier`
- `ByAmountBetRoundQualifier`
- `FixedRateByAmountBetEventQualifier`
- `ITournamentEventQualifier`

## Bounded Compatibility Rewires
- Added localized promo-boundary import bridges so moved promo declarations can still reference unmoved same-package promo types.
- Updated clean callsites for moved promo interfaces/types (`GameServerServiceConfiguration`, `GameServerComponentsConfiguration`, `PrizeWonHandlersFactory`, `PromoCampaignManager`, `CassandraSupportedPromoPlatformsPersister`, plus old-package promo interfaces/wrappers).

## Validation Evidence
- Focused fast gates (`common-promo`, `promo-persisters`, `promo-core`, `common-gs`) remain blocked by pre-existing workspace drift profile.
- Canonical runner (`run-rerun1.sh`) summary:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `step09_retry1`: `SKIP`

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `190`
- Post-batch remaining: `179`
- Reduced total: `2098`
- Batch reduction: `11`
- Burndown: `92.138779%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `52.106183%`
- Core total (01+02): `76.053092%`
- Entire portfolio: `88.026546%`

## ETA Refresh
- Remaining declarations: `179`
- ETA: `~7.3h` (`~0.91` workdays)
