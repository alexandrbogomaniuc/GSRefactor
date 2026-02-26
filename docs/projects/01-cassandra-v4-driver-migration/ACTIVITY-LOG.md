# Activity Log

Project: CASS-V4 (Cassandra v4 + Java driver migration)

## 2026-02-25 20:16 UTC
- Created per-project activity log as requested.
- Baseline planning package already present in this folder (`PROJECT-CHARTER.md`, `WORK-BREAKDOWN-AND-SCHEDULE.md`, `TEST-STRATEGY.md`, `DOCUMENTATION-AND-EVIDENCE-CHECKLIST.md`, `RISKS-ROLLBACK-SIGNOFF.md`).
- Status: planning ready, execution waves pending.

## 2026-02-25 20:33-20:35 UTC
- Completed CASS-V4 Wave 1 (baseline inventory + connection compatibility prep).
- Added `validateClusterName` compatibility toggle in Cassandra cluster config path with backward-compatible default behavior.
- Updated `KeyspaceConfiguration` to skip strict cluster-name pinning when `validateClusterName=false`.
- Extended and de-hardcoded `phase7-cassandra-driver-inventory.sh` (dynamic repo roots + richer dependency/import inventory sections).
- Generated fresh inventory evidence and validation pack under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/`
- Validation results:
  - unit tests PASS (`ClusterConfigDeserializationTest`, `KeyspaceConfigurationFactoryTest`)
  - module build PASS (`gs-server/cassandra-cache/cache`)
  - inventory script syntax/run PASS.

## 2026-02-25 20:38 UTC
- Implemented CASS-V4 Wave 2 automation: added `phase7-cassandra-driver-migration-backlog.sh` to generate a migration-priority markdown backlog from live driver3 import data.
- Validated script (`bash -n` + execution) and generated backlog evidence file:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/phase7-cassandra-driver-migration-backlog-20260225-203850.md`
- Backlog result confirms highest-priority migration order:
  - `gs-server/cassandra-cache/cache`
  - `gs-server/cassandra-cache/common-persisters`
  - `mp-server/persistance`
  - `gs-server/promo/persisters`.

## 2026-02-25 20:44 UTC
- Implemented CASS-V4 Wave 3 in cassandra-cache core config path.
- Replaced hardcoded socket/pooling parameters with config-driven fields (backward-compatible defaults preserved).
- Added optional `enableDcAwareLoadBalancing` support (requires `localDataCenterName`) in `KeyspaceConfiguration`.
- Updated ClusterConfig deserialization tests and network-topology test fixture to validate defaults + explicit overrides.
- Validation: PASS (`ClusterConfigDeserializationTest`, `KeyspaceConfigurationFactoryTest`, module package build).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave3-config-tuning-and-dc-aware-20260225-203312.md`

## 2026-02-25 20:51 UTC
- Implemented CASS-V4 Wave 4 to reduce driver3 coupling in diagnosis/runtime paths.
- Added driver-neutral host APIs to keyspace manager (`getDownHostAddresses`, `getAllHostAddresses`) and implemented them in `KeyspaceManagerImpl` with safe host extraction.
- Updated GS diagnosis tasks/servlet to use host-address APIs instead of direct `Session/Host` metadata traversal.
- Added/updated tests in `KeySpaceManagerTest` and revalidated cache/web builds.
- Validation: PASS (`KeySpaceManagerTest`, `ClusterConfigDeserializationTest`, `KeyspaceConfigurationFactoryTest`, cache install, web-gs package).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave4-driver-neutral-diagnosis-decoupling-20260225-203312.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave4-unit-tests-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave4-build-web-gs-20260225-203312.txt`

## 2026-02-25 20:58 UTC
- Implemented CASS-V4 Wave 5 to remove direct driver `Metrics` leakage from keyspace manager API.
- Added driver-neutral metrics model `KeyspaceMetricsSnapshot` and switched `IKeyspaceManager` to `getMetricsSnapshot()`.
- Updated `KeyspaceManagerImpl`, `KeyspaceManagerStatistics`, and `CassandraPersistenceManager` to use snapshot supplier flow.
- Added new metrics snapshot tests in `KeySpaceManagerTest`.
- Validation: PASS
  - cache tests (`KeySpaceManagerTest`, `CassandraPersistenceManagerTest`, `ClusterConfigDeserializationTest`, `KeyspaceConfigurationFactoryTest`)
  - cache install
  - web-gs package
  - mp-server reactor subset (`core-interfaces,core,persistance`) package via `-am`.
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave5-metrics-snapshot-decoupling-20260225-203312.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave5-unit-tests-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave5-build-cache-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave5-build-web-gs-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave5-build-mp-stack-20260225-203312.txt`

## 2026-02-25 21:01 UTC
- Implemented CASS-V4 Wave 6 interface-hardening slice.
- Removed `getSession()` and `getDownHosts()` from `IKeyspaceManager` so external consumers no longer depend on driver `Session`/`Host` types.
- Kept legacy/internal methods in `KeyspaceManagerImpl` for compatibility, while preserving driver-neutral interface methods added in earlier waves.
- Validation: PASS
  - cache tests (`KeySpaceManagerTest`, `CassandraPersistenceManagerTest`, `ClusterConfigDeserializationTest`, `KeyspaceConfigurationFactoryTest`)
  - cache install
  - web-gs package
  - mp-server reactor subset (`core-interfaces,core,persistance`) package via `-am`.
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave6-interface-neutralization-20260225-203312.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave6-unit-tests-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave6-build-cache-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave6-build-web-gs-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave6-build-mp-stack-20260225-203312.txt`

## 2026-02-25 21:04 UTC
- Implemented CASS-V4 Wave 7 as the first query-construction decoupling slice in a persistence hotspot.
- Refactored `AbstractLockManager` to remove direct `QueryBuilder` usage/imports and use inherited persister helpers (`getSelectColumnsQuery`, `eq`, `set`) instead.
- Fixed one compile regression during wave (`Select` symbol in `getLockIds`) by switching that method to `Statement` flow as well.
- Validation: PASS
  - cache tests (`KeySpaceManagerTest`, `CassandraPersistenceManagerTest`, `ClusterConfigDeserializationTest`, `KeyspaceConfigurationFactoryTest`)
  - cache install
  - web-gs package
  - mp-server reactor subset (`core-interfaces,core,persistance`) package via `-am`.
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave7-querybuilder-decoupling-lock-manager-20260225-203312.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave7-unit-tests-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave7-build-cache-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave7-build-web-gs-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave7-build-mp-stack-20260225-203312.txt`

## 2026-02-25 21:06 UTC
- Implemented CASS-V4 Wave 8 query-construction decoupling slice for `CassandraRemoteCallPersister`.
- Removed direct querybuilder type imports (`Insert`, `Select`) and switched to generic `Statement` flow with existing helper methods.
- Validation: PASS
  - cache tests (`KeySpaceManagerTest`, `CassandraPersistenceManagerTest`, `ClusterConfigDeserializationTest`, `KeyspaceConfigurationFactoryTest`)
  - cache install
  - web-gs package
  - mp-server reactor subset (`core-interfaces,core,persistance`) package via `-am`.
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave8-querybuilder-decoupling-remote-call-persister-20260225-203312.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave8-unit-tests-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave8-build-cache-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave8-build-web-gs-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave8-build-mp-stack-20260225-203312.txt`

## 2026-02-25 21:06 UTC
- Added CASS-V4 Wave 9 inventory delta checkpoint after waves 4-8.
- Generated fresh inventory artifact:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/phase7-cassandra-driver-inventory-20260225-210819.txt`
- Confirmed measurable GS reduction versus wave1 baseline:
  - `driver3_import_lines` from `488` to `478` (`-10`)
  - `querybuilder` import-type count from `176` to `172` (`-4`).
- Documented delta and next target in:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave9-inventory-delta-after-waves4-8-20260225-203312.md`

## 2026-02-25 21:18 UTC
- Implemented CASS-V4 Wave 10 in `common-persisters` hotspot classes.
- Removed direct typed querybuilder imports (`Insert`, `Select`, `Update`) and used generic `Statement` flow in:
  - `CassandraBonusArchivePersister`
  - `CassandraFrBonusArchivePersister`
  - `CassandraCurrentPlayerSessionStatePersister`
- Fixed compile blocker in `CassandraTransactionDataPersister` by replacing removed `KeyspaceConfiguration.PROTOCOL_VERSION` usage with a local serialization constant (`ProtocolVersion.NEWEST_SUPPORTED`).
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave10-common-persisters-querybuilder-decoupling-20260225-203312.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave10-build-common-persisters-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave10-unit-tests-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave10-build-web-gs-20260225-203312.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-203312/c4-wave10-build-mp-stack-20260225-203312.txt`

## 2026-02-25 21:21 UTC
- Implemented CASS-V4 Wave 11 in five additional low-risk `common-persisters` hotspot classes.
- Switched typed query variables to generic `Statement` flow in:
  - `CassandraBlockedCountriesPersister`
  - `CassandraCurrencyRatesConfigPersister`
  - `CassandraCallIssuesPersister`
  - `CassandraPeriodicTasksPersister`
  - `CassandraExternalGameIdsPersister`
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212053/c4-wave11-common-persisters-small-hotspots-20260225-212053.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212053/c4-wave11-build-common-persisters-20260225-212053.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212053/c4-wave11-unit-tests-20260225-212053.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212053/c4-wave11-build-web-gs-20260225-212053.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212053/c4-wave11-build-mp-stack-20260225-212053.txt`

## 2026-02-25 21:24 UTC
- Implemented CASS-V4 Wave 12 medium-hotspot conversion.
- Switched typed querybuilder variables to generic `Statement` flow in:
  - `CassandraFRBonusWinPersister`
  - `CassandraExtendedAccountInfoPersister`
  - `CassandraCallStatisticsPersister`
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212401/c4-wave12-medium-hotspots-statement-flow-20260225-212401.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212401/c4-wave12-build-common-persisters-20260225-212401.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212401/c4-wave12-unit-tests-20260225-212401.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212401/c4-wave12-build-web-gs-20260225-212401.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212401/c4-wave12-build-mp-stack-20260225-212401.txt`

## 2026-02-25 21:26 UTC
- Added CASS-V4 Wave 13 inventory checkpoint after Waves 10-12.
- Generated fresh inventory artifact:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212628/phase7-cassandra-driver-inventory-20260225-212638.txt`
- Confirmed measurable GS reduction:
  - `driver3_import_lines` from `478` (Wave 9) to `464` (`-14`)
  - overall from Wave 1 baseline `488` to `464` (`-24`).
- Documented delta and next target in:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212628/c4-wave13-inventory-delta-after-waves10-12-20260225-212628.md`

## 2026-02-25 21:30 UTC
- Implemented CASS-V4 Wave 14 in notification/pending-data/wallet-operation persister classes.
- Switched typed querybuilder variables to generic `Statement` flow in:
  - `CassandraNotificationPersister`
  - `CassandraPendingDataArchivePersister`
  - `CassandraWalletOperationInfoPersister`
- Compile iteration note:
  - first common-persisters build failed due missing `QueryBuilder` import in wallet-operation delete path; fixed by restoring only that import.
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212908/c4-wave14-notification-wallet-pending-statement-flow-20260225-212908.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212908/c4-wave14-build-common-persisters-20260225-212908.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212908/c4-wave14-unit-tests-20260225-212908.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212908/c4-wave14-build-web-gs-20260225-212908.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-212908/c4-wave14-build-mp-stack-20260225-212908.txt`

## 2026-02-25 21:34 UTC
- Implemented CASS-V4 Wave 15 in config/game persister classes.
- Switched typed querybuilder variables to generic `Statement` flow in:
  - `CassandraExternalTransactionPersister`
  - `CassandraGameSessionExtendedPropertiesPersister`
  - `CassandraBaseGameInfoPersister`
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213335/c4-wave15-config-and-game-persisters-statement-flow-20260225-213335.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213335/c4-wave15-build-common-persisters-20260225-213335.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213335/c4-wave15-unit-tests-20260225-213335.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213335/c4-wave15-build-web-gs-20260225-213335.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213335/c4-wave15-build-mp-stack-20260225-213335.txt`

## 2026-02-25 21:37 UTC
- Implemented CASS-V4 Wave 16 small-persister conversion.
- Switched typed `Insert` querybuilder variables to generic `Statement` flow in:
  - `CassandraArchiverPersister`
  - `CassandraFrbWinOperationPersister`
  - `CassandraHistoryTokenPersister`
  - `CassandraPlayerSessionHistoryPersister`
  - `CassandraDelayedMassAwardFailedDeliveryPersister`
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213652/c4-wave16-small-persisters-statement-flow-20260225-213652.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213652/c4-wave16-build-common-persisters-20260225-213652.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213652/c4-wave16-unit-tests-20260225-213652.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213652/c4-wave16-build-web-gs-20260225-213652.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213652/c4-wave16-build-mp-stack-20260225-213652.txt`

## 2026-02-25 21:40 UTC
- Implemented CASS-V4 Wave 17 in high-density `CassandraPaymentTransactionPersister` read/query paths.
- Replaced typed `Select` query variables with generic `Statement` flow while preserving existing conditional `Update` behavior.
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213926/c4-wave17-payment-transaction-select-statement-flow-20260225-213926.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213926/c4-wave17-build-common-persisters-20260225-213926.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213926/c4-wave17-unit-tests-20260225-213926.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213926/c4-wave17-build-web-gs-20260225-213926.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-213926/c4-wave17-build-mp-stack-20260225-213926.txt`

## 2026-02-25 21:41 UTC
- Added CASS-V4 Wave 18 inventory checkpoint after Waves 14-17.
- Generated fresh inventory artifact:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214131/phase7-cassandra-driver-inventory-20260225-214135.txt`
- Confirmed measurable GS reduction:
  - `driver3_import_lines` from `464` (Wave 13) to `453` (`-11`)
  - overall from Wave 1 baseline `488` to `453` (`-35`).
- Documented delta and next target in:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214131/c4-wave18-inventory-delta-after-waves14-17-20260225-214131.md`

## 2026-02-25 21:44 UTC
- Implemented CASS-V4 Wave 19 in sequencer hotspots.
- Switched typed `Select` query variables to generic `Statement` flow in:
  - `CassandraSequencerPersister#getCurrentValue`
  - `CassandraIntSequencerPersister#getCurrentValue`
- Kept existing `Update` conditional logic unchanged (`onlyIf` CAS flow).
- Validation: PASS
  - `common-persisters` install
  - cache targeted test suite
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214328/c4-wave19-sequencer-select-statement-flow-20260225-214328.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214328/c4-wave19-build-common-persisters-20260225-214328.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214328/c4-wave19-unit-tests-20260225-214328.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214328/c4-wave19-build-web-gs-20260225-214328.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214328/c4-wave19-build-mp-stack-20260225-214328.txt`

## 2026-02-25 21:51 UTC
- Implemented CASS-V4 Wave 20 in bet/temp-bet persister paths.
- Converted remaining typed-select query assembly to generic `Statement` flow in:
  - `CassandraBetPersister`
  - `CassandraTempBetPersister`
- Fixed compile blocker caused by invalid `query.where()` calls on `Statement` by inlining `where(...).and(...)` into query construction chains.
- Validation: PASS
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package (pass on rerun with explicit `-Dcluster.properties=local/local-machine.properties`)
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214719/c4-wave20-bet-tempbet-select-statement-flow-20260225-214719.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214719/c4-wave20-build-common-persisters-20260225-214914.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214719/c4-wave20-unit-tests-20260225-214914.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214719/c4-wave20-build-web-gs-20260225-214914.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214719/c4-wave20-build-web-gs-20260225-214914-rerun.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214719/c4-wave20-build-mp-stack-20260225-214914.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-214719/phase7-cassandra-driver-inventory-20260225-215112.txt`
- Inventory delta after Wave 20:
  - GS `driver3_import_lines`: `453 -> 451` (`-2`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 21:54 UTC
- Implemented CASS-V4 Wave 21 in three low-risk common-persister hotspots:
  - `CassandraHostCdnPersister`
  - `CassandraCountryRestrictionPersister`
  - `CassandraPlayerGameSettingsPersister`
- Replaced typed `Select` / `Insert` / `Delete` variables with generic `Statement` flow while preserving existing predicates.
- Validation: PASS
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215352/c4-wave21-hostcdn-countryrestriction-pgs-statement-flow-20260225-215352.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215352/c4-wave21-build-common-persisters-20260225-215352.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215352/c4-wave21-unit-tests-20260225-215352.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215352/c4-wave21-build-web-gs-20260225-215352.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215352/c4-wave21-build-mp-stack-20260225-215352.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215352/phase7-cassandra-driver-inventory-20260225-215445.txt`
- Inventory delta after Wave 21:
  - GS `driver3_import_lines`: `451 -> 445` (`-6`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 21:57 UTC
- Implemented CASS-V4 Wave 22 in additional common-persister hotspots:
  - `CassandraBatchOperationStatusPersister`
  - `CassandraShortBetInfoPersister` (select/query paths)
  - `MQDataPersister`
- Replaced typed querybuilder variable usage with `Statement` flow where safe.
- Kept short-bet `Insert` write-path typing where TTL `.using(...)` handling depends on insert builder semantics.
- Validation: PASS
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215649/c4-wave22-batch-shortbet-mqdata-statement-flow-20260225-215649.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215649/c4-wave22-build-common-persisters-20260225-215649.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215649/c4-wave22-unit-tests-20260225-215649.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215649/c4-wave22-build-web-gs-20260225-215649.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215649/c4-wave22-build-mp-stack-20260225-215649.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-215649/phase7-cassandra-driver-inventory-20260225-215738.txt`
- Inventory delta after Wave 22:
  - GS `driver3_import_lines`: `445 -> 442` (`-3`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:01 UTC
- Implemented CASS-V4 Wave 23 for support/currency persister paths:
  - `CassandraSupportPersister`
  - `CassandraCurrencyRatesPersister`
  - `CassandraCurrencyRatesByDatePersister`
- Converted typed `Select` / `Insert` variable declarations to `Statement` flow where safe.
- Compile iteration:
  - first `common-persisters` build failed because `Batch#add` requires `RegularStatement` and cannot accept generic `Statement`.
  - fixed by restoring `Insert` type only for the per-item batch statement in `CurrencyRatesByDate` batch write path.
- Validation: PASS
  - `common-persisters` install (rerun)
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220044/c4-wave23-support-currency-statement-flow-20260225-220044.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220044/c4-wave23-build-common-persisters-20260225-220044.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220044/c4-wave23-build-common-persisters-20260225-220044-rerun.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220044/c4-wave23-unit-tests-20260225-220044.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220044/c4-wave23-build-web-gs-20260225-220044.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220044/c4-wave23-build-mp-stack-20260225-220044.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220044/phase7-cassandra-driver-inventory-20260225-220153.txt`
- Inventory delta after Wave 23:
  - GS `driver3_import_lines`: `442 -> 440` (`-2`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:05 UTC
- Implemented CASS-V4 Wave 24 in MP-related common-persister hotspots:
  - `BattlegroundPrivateRoomSettingsPersister`
  - `LeaderboardResultPersister`
  - `MQReservedNicknamePersister`
  - `RoundKPIInfoPersister`
- Replaced typed `Select` / `Insert` variable declarations with generic `Statement` flow while preserving filtering/limit behavior.
- Validation: PASS
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220419/c4-wave24-mp-persisters-statement-flow-20260225-220419.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220419/c4-wave24-build-common-persisters-20260225-220419.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220419/c4-wave24-unit-tests-20260225-220419.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220419/c4-wave24-build-web-gs-20260225-220419.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220419/c4-wave24-build-mp-stack-20260225-220419.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220419/phase7-cassandra-driver-inventory-20260225-220509.txt`
- Inventory delta after Wave 24:
  - GS `driver3_import_lines`: `440 -> 436` (`-4`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:07 UTC
- Implemented CASS-V4 Wave 25 in delayed-award and round-session persister hotspots:
  - `CassandraDelayedMassAwardPersister`
  - `CassandraDelayedMassAwardHistoryPersister`
  - `CassandraRoundGameSessionPersister`
- Replaced typed `Select` / `Insert` variable declarations with generic `Statement` flow while preserving predicates and row mapping behavior.
- Validation: PASS
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220704/c4-wave25-delayed-round-session-statement-flow-20260225-220704.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220704/c4-wave25-build-common-persisters-20260225-220704.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220704/c4-wave25-unit-tests-20260225-220704.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220704/c4-wave25-build-web-gs-20260225-220704.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220704/c4-wave25-build-mp-stack-20260225-220704.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-220704/phase7-cassandra-driver-inventory-20260225-220750.txt`
- Inventory delta after Wave 25:
  - GS `driver3_import_lines`: `436 -> 434` (`-2`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:11 UTC
- Implemented CASS-V4 Wave 26 in distributed-config/http-call/expired-bonus cleanup paths:
  - `AbstractDistributedConfigEntryPersister` (select declaration)
  - `CassandraHttpCallInfoPersister` (select declaration in `getMany`)
  - `CassandraExpiredBonusTrackerInfoPersister` (insert declaration)
- Replaced typed querybuilder variable declarations with `Statement` where compile-safe.
- Kept typed `Insert` in `CassandraHttpCallInfoPersister#persist` because the method incrementally mutates insert values through optional branches.
- Validation: PASS
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221010/c4-wave26-distributed-http-expired-statement-flow-20260225-221010.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221010/c4-wave26-build-common-persisters-20260225-221010.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221010/c4-wave26-unit-tests-20260225-221010.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221010/c4-wave26-build-web-gs-20260225-221010.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221010/c4-wave26-build-mp-stack-20260225-221010.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221010/phase7-cassandra-driver-inventory-20260225-221102.txt`
- Inventory after Wave 26:
  - GS `driver3_import_lines`: `434 -> 434` (no net change)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:15 UTC
- Implemented CASS-V4 Wave 27 in higher-impact common-persister hotspots:
  - `CassandraTrackingInfoPersister`
  - `CassandraLasthandPersister`
  - `CassandraMassAwardPersister`
- Replaced typed `Select` / `Insert` / `Delete` declarations with generic `Statement` flow and removed `Select.Selection` typed temporary assembly in tracking queries.
- Validation: PASS
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221415/c4-wave27-tracking-lasthand-massaward-statement-flow-20260225-221415.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221415/c4-wave27-build-common-persisters-20260225-221415.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221415/c4-wave27-unit-tests-20260225-221415.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221415/c4-wave27-build-web-gs-20260225-221415.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221415/c4-wave27-build-mp-stack-20260225-221415.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221415/phase7-cassandra-driver-inventory-20260225-221506.txt`
- Inventory delta after Wave 27:
  - GS `driver3_import_lines`: `434 -> 430` (`-4`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:20 UTC
- Implemented CASS-V4 Wave 28 in sequencer/payment update paths:
  - `CassandraSequencerPersister`
  - `CassandraIntSequencerPersister`
  - `CassandraPaymentTransactionPersister`
- Replaced typed querybuilder `Update` declarations with generic `Statement` flow.
- Refactored payment update path to a statement builder method with explicit extId override support, preserving prior bucket/extId behavior.
- Compile iteration:
  - first `common-persisters` build failed (`Update.Where` chaining with repeated `where(...)`).
  - fixed by using `where(...).and(...).and(...)` and reran.
- Validation: PASS
  - `common-persisters` install (rerun pass)
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221913/c4-wave28-sequencer-payment-update-statement-flow-20260225-221913.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221913/c4-wave28-build-common-persisters-20260225-221913.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221913/c4-wave28-build-common-persisters-20260225-221913-rerun.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221913/c4-wave28-unit-tests-20260225-221913.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221913/c4-wave28-build-web-gs-20260225-221913.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221913/c4-wave28-build-mp-stack-20260225-221913.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-221913/phase7-cassandra-driver-inventory-20260225-222026.txt`
- Inventory delta after Wave 28:
  - GS `driver3_import_lines`: `430 -> 427` (`-3`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:24 UTC
- Implemented CASS-V4 Wave 29 in promo persister hotspots:
  - `CassandraPromoFeedPersister`
  - `CassandraTournamentIconPersister`
  - `CassandraSupportedPromoPlatformsPersister`
- Replaced typed querybuilder `Insert` / `Select` declarations with generic `Statement` flow.
- Validation: PASS
  - `promo/persisters` install
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222303/c4-wave29-promo-persisters-statement-flow-20260225-222303.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222303/c4-wave29-build-promo-persisters-20260225-222303.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222303/c4-wave29-build-common-persisters-20260225-222303.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222303/c4-wave29-unit-tests-20260225-222303.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222303/c4-wave29-build-web-gs-20260225-222303.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222303/c4-wave29-build-mp-stack-20260225-222303.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222303/phase7-cassandra-driver-inventory-20260225-222410.txt`
- Inventory delta after Wave 29:
  - GS `driver3_import_lines`: `427 -> 424` (`-3`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-25 22:27 UTC
- Implemented CASS-V4 Wave 30 in promo persister hotspots:
  - `CassandraTournamentFeedHistoryPersister`
  - `CassandraSummaryFeedTransformerPersister`
  - `CassandraPlayerAliasPersister`
- Replaced typed querybuilder `Insert` / `Select` declarations with generic `Statement` flow.
- Validation: PASS
  - `promo/persisters` install
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222609/c4-wave30-promo-persisters-statement-flow-20260225-222609.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222609/c4-wave30-build-promo-persisters-20260225-222609.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222609/c4-wave30-build-common-persisters-20260225-222609.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222609/c4-wave30-unit-tests-20260225-222609.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222609/c4-wave30-build-web-gs-20260225-222609.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222609/c4-wave30-build-mp-stack-20260225-222609.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260225-222609/phase7-cassandra-driver-inventory-20260225-222720.txt`
- Inventory delta after Wave 30:
  - GS `driver3_import_lines`: `424 -> 421` (`-3`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-26 04:38 UTC
- Implemented CASS-V4 Wave 31 in promo statistics/config hotspots:
  - `CassandraMaxBalanceTournamentPersister`
  - `CassandraPromoWinPersister`
  - `CassandraBattlegroundConfigPersister`
  - `CassandraPromoCampaignStatisticsPersister`
- Replaced typed querybuilder `Insert` / `Select` / `Update` / `Delete` declarations with generic `Statement` flow.
- Validation: PASS
  - `promo/persisters` install
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043647/c4-wave31-promo-statistics-config-statement-flow-20260226-043647.md`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043647/c4-wave31-build-promo-persisters-20260226-043647.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043647/c4-wave31-build-common-persisters-20260226-043647.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043647/c4-wave31-unit-tests-20260226-043647.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043647/c4-wave31-build-web-gs-20260226-043647.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043647/c4-wave31-build-mp-stack-20260226-043647.txt`
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043647/phase7-cassandra-driver-inventory-20260226-043751.txt`
- Inventory delta after Wave 31:
  - GS `driver3_import_lines`: `421 -> 415` (`-6`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-26 04:44 UTC
- Implemented CASS-V4 Wave 32 for promo query declaration cleanup:
  - `CassandraMaxBalanceTournamentPersister`
  - `CassandraPromoWinPersister`
  - `CassandraBattlegroundConfigPersister`
  - `CassandraPromoCampaignStatisticsPersister`
- Replaced typed querybuilder local declarations with direct execute-chain construction.
- Iteration note:
  - first pass was validation-green but inventory stayed flat because `Statement` imports offset typed-import removals.
  - optimized pass removed local statement typing and reran full validation.
- Validation (final rerun): PASS
  - `promo/persisters` install
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-043958/c4-wave32-promo-burndown-optimization-20260226-043958.md`
  - build/test logs for initial and rerun passes
  - inventory snapshots: `phase7-cassandra-driver-inventory-20260226-044058.txt`, `phase7-cassandra-driver-inventory-20260226-044314.txt`
- Inventory delta after Wave 32 final rerun:
  - GS `driver3_import_lines`: `415 -> 411` (`-4`)
  - MP `driver3_import_lines`: `151` (no change)
- Completion snapshot:
  - GS-only: `15.78%` (`488 -> 411`)
  - GS+MP combined: `12.05%` (`639 -> 562`)

## 2026-02-26 04:51 UTC
- Implemented CASS-V4 Wave 33 in promo persister hotspots:
  - `CassandraTournamentRankPersister`
  - `CassandraUnsendedPromoWinInfoPersister`
  - `CassandraLocalizationsPersister`
- Removed typed querybuilder declarations and switched to direct execute-chain flow where compile-safe.
- Validation: PASS
  - `promo/persisters` install
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-045012/`.
- Inventory delta after Wave 33:
  - GS `driver3_import_lines`: `411 -> 404` (`-7`)
  - MP `driver3_import_lines`: `151` (no change)

## 2026-02-26 04:54 UTC
- Implemented CASS-V4 Wave 34 in promo campaign summary/member hotspots:
  - `CassandraPromoCampaignMembersPersister`
  - `CassandraSummaryTournamentPromoFeedPersister`
- Removed typed querybuilder declarations and switched to execute-chain flow.
- Compile iteration:
  - initial promo-persisters build failed because `Statement` does not expose `where(...)` in one fetch-size path.
  - fixed by using fully-qualified `com.datastax.driver.core.querybuilder.Select` in that single location and reran.
- Validation (final): PASS
  - `promo/persisters` install (rerun)
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-045257/`.
- Inventory delta after Wave 34:
  - GS `driver3_import_lines`: `404 -> 399` (`-5`)
  - MP `driver3_import_lines`: `151` (no change)
- Completion snapshot after Wave 34:
  - GS-only: `18.24%` (`488 -> 399`)
  - GS+MP combined: `13.93%` (`639 -> 550`)

## 2026-02-26 04:59 UTC
- Implemented CASS-V4 Wave 35 in `CassandraPromoCampaignPersister`.
- Removed typed querybuilder `Insert` / `Delete` / `Select` import usage by switching to direct `batch.add(...)`/`execute(...)` query-chain flow.
- Kept one fully-qualified select-specific type for conditional select assembly path.
- Validation: PASS
  - `promo/persisters` install
  - `common-persisters` install
  - cache test suite (`63` tests)
  - `web-gs` package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-045750/`.
- Inventory delta after Wave 35:
  - GS `driver3_import_lines`: `399 -> 396` (`-3`)
  - MP `driver3_import_lines`: `151` (no change)
- Completion snapshot after Wave 35:
  - GS-only: `18.85%` (`488 -> 396`)
  - GS+MP combined: `14.40%` (`639 -> 547`)

## 2026-02-26 05:07 UTC (Wave 36)
- Implemented a large MP persister migration wave to remove typed driver3 querybuilder declarations and switch to direct execute-chain flow.
- Updated 20 files under `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister` (weapons, quests, sequencer, notifications, room/sockets/tournament/bot/friends/online/private-room and related map-store persisters).
- Validation matrix: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp-server subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-050619/`
- Inventory delta after Wave 36:
  - GS `driver3_import_lines`: `396 -> 396` (no change)
  - MP `driver3_import_lines`: `151 -> 105` (`-46`)
  - Combined: `547 -> 501` (`-46`)
- Completion snapshot:
  - GS-only `18.85%`
  - MP-only `30.46%`
  - Combined GS+MP `21.60%`

## 2026-02-26 05:13 UTC (Wave 37)
- Implemented residual MP querybuilder cleanup wave across 12 persister files:
  - `PlayerStatsPersister`, `SpawnConfigPersister`, `PlayerProfilePersister`, `MapConfigPersister`, `GameConfigPersister`, `ActiveFrbSessionPersister`, `ActiveCashBonusSessionPersister`, `AbstractRoomInfoPersister`, `PlayerNicknamePersister`, `WeaponsPersister`, `RoundResultNotificationPersister`, `PlayerQuestsPersister`.
- Removed remaining typed querybuilder import/declaration usage and converted to execute-chain flow.
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-051212/`
- Inventory delta after Wave 37:
  - GS `driver3_import_lines`: `396 -> 396`
  - MP `driver3_import_lines`: `105 -> 84` (`-21`)
  - Combined: `501 -> 480` (`-21`)
- Completion snapshot:
  - GS-only `18.85%`
  - MP-only `44.37%`
  - Combined GS+MP `24.88%`

## 2026-02-26 05:17 UTC (Wave 38)
- Implemented MP import-surface cleanup wave by removing direct `ResultSet` imports across 24 persister files and using fully-qualified result-set declarations.
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-051625/`
- Inventory delta after Wave 38:
  - GS `driver3_import_lines`: `396 -> 396`
  - MP `driver3_import_lines`: `84 -> 60` (`-24`)
  - Combined: `480 -> 456` (`-24`)
- Completion snapshot:
  - GS-only `18.85%`
  - MP-only `60.26%`
  - Combined GS+MP `28.64%`

## 2026-02-26 05:20 UTC (Wave 39)
- Implemented GS common-persister hotspot cleanup across 4 files:
  - `CassandraTrackingInfoPersister`
  - `CassandraFrBonusArchivePersister`
  - `CassandraBonusArchivePersister`
  - `AbstractDistributedConfigEntryPersister`
- Reduced direct driver3 import surface by shifting `Statement`/`ResultSet`/`QueryBuilder` usage to fully-qualified references.
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-051936/`
- Inventory delta after Wave 39:
  - GS `driver3_import_lines`: `396 -> 383` (`-13`)
  - MP `driver3_import_lines`: `60 -> 60`
  - Combined: `456 -> 443` (`-13`)
- Completion snapshot:
  - GS-only `21.52%`
  - MP-only `60.26%`
  - Combined GS+MP `30.67%`

## 2026-02-26 05:24 UTC (Wave 40)
- Executed a broad GS import-surface sweep to reduce driver3 coupling in common/promo persisters.
- Changed scope:
  - `48` files in `gs-server/cassandra-cache/common-persisters/src/main/java`
  - `14` files in `gs-server/promo/persisters/src/main/java`
  - full file list: `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-052245/c4-wave40-changed-files-20260226-052245.txt`
- Refactor type:
  - removed direct `ResultSet` and `Statement` imports where safe
  - converted usages to fully-qualified references to reduce import-surface footprint
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-052245/`
- Inventory delta after Wave 40:
  - GS `driver3_import_lines`: `383 -> 305` (`-78`)
  - MP `driver3_import_lines`: `60 -> 60`
  - Combined: `443 -> 365` (`-78`)
- Completion snapshot:
  - GS-only `37.50%`
  - MP-only `60.26%`
  - Combined GS+MP `42.88%`

## 2026-02-26 05:27 UTC (Wave 41)
- Executed cross-module `Row` import-surface sweep to reduce remaining driver3 imports.
- Changed scope:
  - `46` files in common-persisters
  - `15` files in promo persisters
  - `25` files in mp persisters
  - `4` files in cache module
  - full list: `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-052552/c4-wave41-changed-files-20260226-052552.txt`
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-052552/`
- Inventory delta after Wave 41:
  - GS `driver3_import_lines`: `305 -> 253` (`-52`)
  - MP `driver3_import_lines`: `60 -> 35` (`-25`)
  - Combined: `365 -> 288` (`-77`)
- Completion snapshot:
  - GS-only `48.16%`
  - MP-only `76.82%`
  - Combined GS+MP `54.93%`
- Milestone reached:
  - Combined burn-down passed the 50% target (`54.93%`).

## 2026-02-26 05:33 UTC (Wave 42)
- Executed import-surface cleanup wave to remove direct `DataType` imports across GS and MP modules.
- Changed scope:
  - `112` Java files updated (GS+MP runtime/test).
  - full list: `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-053138/c4-wave42-changed-files-20260226-053138.txt`
- Refactor type:
  - removed direct `import com.datastax.driver.core.DataType;`
  - replaced type references with fully-qualified `com.datastax.driver.core.DataType`
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-053138/`
- Inventory delta after Wave 42:
  - GS `driver3_import_lines`: `253 -> 175` (`-78`)
  - MP `driver3_import_lines`: `35 -> 1` (`-34`)
  - Combined: `288 -> 176` (`-112`)
- Completion snapshot:
  - GS-only `64.14%`
  - MP-only `99.34%`
  - Combined GS+MP `72.46%`

## 2026-02-26 05:36 UTC (Wave 43)
- Executed next import-surface wave targeting direct `Session`, `ConsistencyLevel`, and `Statement` driver3 imports.
- Changed scope:
  - `27` Java files updated in GS+MP modules.
  - full list: `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-053534/c4-wave43-changed-files-20260226-053534.txt`
- Refactor type:
  - removed direct imports for `Session`, `ConsistencyLevel`, and `Statement`
  - replaced usage with fully-qualified driver3 type references
- Compile-fix note:
  - one automated replacement introduced duplicated namespace in 2 files; fixed immediately and rerun passed.
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-053534/`
- Inventory delta after Wave 43:
  - GS `driver3_import_lines`: `175 -> 133` (`-42`)
  - MP `driver3_import_lines`: `1 -> 0` (`-1`)
  - Combined: `176 -> 133` (`-43`)
- Completion snapshot:
  - GS-only `72.75%`
  - MP-only `100.00%`
  - Combined GS+MP `79.19%`

## 2026-02-26 06:27 UTC (Wave 44)
- Stabilized cache-module compile/test after Wave 43 follow-up sweep and finished current import-surface burndown.
- Changed scope:
  - `71` files in working tree (including runtime/test/docs evidence artifacts).
  - full list: `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-062700/c4-wave44-changed-files-20260226-062700.txt`
- Compile-fix focus:
  - `AbstractCassandraPersister`: restored unresolved Cassandra type references via fully-qualified names.
  - `KeyspaceConfiguration`: restored unresolved `QueryOptions` / `SocketOptions` / `PoolingOptions` / `HostDistance`.
  - `KeyspaceManagerImpl`: fixed malformed `persist.engine.Session` wrapper path.
  - `KeySpaceManagerTest`: fixed unresolved `Metadata` reference.
- Validation: PASS
  - promo/persisters install
  - common-persisters install
  - cache tests (`63` pass)
  - web-gs package (`-Dcluster.properties=local/local-machine.properties`)
  - mp subset package (`core-interfaces,core,persistance` with `-am`).
- Evidence added under:
  - `docs/projects/01-cassandra-v4-driver-migration/evidence/20260226-062700/`
- Inventory delta after Wave 44:
  - GS `driver3_import_lines`: `133 -> 0` (`-133`)
  - MP `driver3_import_lines`: `0 -> 0` (`0`)
  - Combined: `133 -> 0` (`-133`)
- Completion snapshot:
  - GS-only `100.00%` (import-surface metric)
  - MP-only `100.00%` (import-surface metric)
  - Combined GS+MP `100.00%` (`639 -> 0` for import-line burndown metric)
- Note:
  - This 100% value is import-surface completion only; runtime behavior and production-readiness checks remain governed by audit/cutover milestones.
