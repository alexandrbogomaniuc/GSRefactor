# Project 02 Hard-Cut Live Batch W Report (Common Cache/Data 25)

## Timestamp
- 2026-03-01 10:29 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `25` declarations
- Retained declaration moves: `25`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-102953-hardcut-live-batchW-common-cachedata25`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `AccountInfo`
2. `BankInfo`
3. `SubCasino`
4. `PlayerBet`
5. `BaseBonus`
6. `BaseMassAward`
7. `Bonus`
8. `BonusStatus`
9. `FRBonus`
10. `Currency`
11. `BaseGameInfo`
12. `BaseGameInfoTemplate`
13. `GameSession`
14. `SessionInfo`
15. `GameServerConfigTemplate`
16. `MessageManager`
17. `BankInfoCache`
18. `BaseGameCache`
19. `BaseGameInfoTemplateCache`
20. `CurrencyCache`
21. `LoadBalancerCache`
22. `SubCasinoCache`
23. `SubCasinoGroupCache`
24. `DomainSession`
25. `SessionHelper`

## Bounded Compatibility Rewires
- Package-only migration for retained common cache/data/session/config targets.
- Bounded import rewires inside retained targets only:
  - rewired `import com.dgphoenix...` to `import com.abs...` only when imported source already exists with `package com.abs`.
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

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `57`
- Post-batch remaining: `32`
- Reduced total: `2245`
- Batch reduction: `25`
- Burndown: `98.594642%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `54.091889%`
- Core total (01+02): `77.045945%`
- Entire portfolio: `88.522972%`

## ETA Refresh
- Remaining declarations: `32`
- ETA: `~1.3h` (`~0.17` workdays)
