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
