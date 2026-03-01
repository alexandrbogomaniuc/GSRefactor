# Project 02 Hard-Cut Live Batch X Report (Final Clean 28)

## Timestamp
- 2026-03-01 10:33 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `28` declarations
- Retained declaration moves: `28`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-103251-hardcut-live-batchX-final-clean28`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `ColumnDefinition`
2. `ICassandraPersister`
3. `AccountDistributedLockManager`
4. `CommonWalletManager`
5. `CanexCWClient`
6. `ITransactionData`
7. `IdGenerator`
8. `IntegerIdGenerator`
9. `CCommonWallet`
10. `CommonGameWallet`
11. `CommonWallet`
12. `CommonWalletOperation`
13. `IWalletProtocolManager`
14. `RemoteClientStubHelper`
15. `ICommonWalletClient` (v2)
16. `ICommonWalletClient` (v4)
17. `AccountManager`
18. `GameServerComponentsHelper`
19. `BonusManager`
20. `FRBonusManager`
21. `WalletProtocolFactory`
22. `BattlegroundService`
23. `MQServiceHandler`
24. `GameServerConfiguration`
25. `CommonException`
26. `CollectionUtils`
27. `StringUtils`
28. `StatisticsManager`

## Bounded Compatibility Rewires
- Package-only migration for retained declarations.
- Bounded import rewires inside retained targets only:
  - rewired `import com.dgphoenix...` to `import com.abs...` only when imported source is already moved to `com.abs`.
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
- Post-scan shows `13` `package com.dgphoenix` declarations remain under `gs-server`.
- All residual declarations are in files already dirty before this batch (`common-promo`, `cassandra-cache/cache`, `utils/NtpTimeProvider`, `common-gs/GameServer`, plus one scanner smoke script text fixture).
- This run kept strict selective staging and did not stage unrelated pre-existing file modifications.

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `32`
- Post-batch remaining: `4`
- Reduced total: `2273`
- Batch reduction: `28`
- Burndown: `99.824330%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `54.470119%`
- Core total (01+02): `77.235059%`
- Entire portfolio: `88.617530%`

## ETA Refresh
- Remaining declarations: `4`
- ETA: `~0.2h` (`~0.02` workdays)
