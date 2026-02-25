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
