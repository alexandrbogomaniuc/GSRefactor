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
