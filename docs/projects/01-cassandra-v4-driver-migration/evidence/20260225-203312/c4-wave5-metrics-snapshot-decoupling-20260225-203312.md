# CASS-V4 Wave 5: Driver-neutral metrics snapshot for runtime statistics

Date (UTC): 2026-02-25 20:58
Project: `CASS-V4`

## Scope
Remove direct Cassandra driver `Metrics` type leakage from `IKeyspaceManager` and keep statistics output working through a driver-neutral snapshot model.

## Implementation
### 1) Keyspace manager API now exposes a driver-neutral metrics snapshot
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/IKeyspaceManager.java`

Change:
- replaced `Metrics getMetrics()` with `KeyspaceMetricsSnapshot getMetricsSnapshot()`.

### 2) Added snapshot model for Cassandra metrics
Added:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/KeyspaceMetricsSnapshot.java`

Capabilities:
- captures known hosts, connections, queue depth, request rates, latency summary, and driver error counters.
- supports `unavailable()` fallback when metrics are not ready.

### 3) Keyspace manager implementation switched to snapshot generation
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/KeyspaceManagerImpl.java`

Change:
- now returns `KeyspaceMetricsSnapshot.from(cluster.getMetrics())` with init/cluster guards.

### 4) Runtime statistics builder no longer depends on driver `Metrics`
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/KeyspaceManagerStatistics.java`
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/CassandraPersistenceManager.java`

Changes:
- `KeyspaceManagerStatistics` now consumes `Supplier<KeyspaceMetricsSnapshot>`.
- persistence manager registers statistics getter using `keyspaceManager::getMetricsSnapshot`.

### 5) Test coverage extended
Updated:
- `gs-server/cassandra-cache/cache/src/test/java/com/dgphoenix/casino/cassandra/KeySpaceManagerTest.java`

Added tests:
- `testMetricsSnapshotUnavailableBeforeInit`
- `testMetricsSnapshotAfterInit`

## Validation
- Unit tests: PASS
  - `KeySpaceManagerTest`
  - `CassandraPersistenceManagerTest`
  - `ClusterConfigDeserializationTest`
  - `KeyspaceConfigurationFactoryTest`
- Build/package: PASS
  - `gs-server/cassandra-cache/cache` install
  - `gs-server/game-server/web-gs` package
  - `mp-server` reactor subset (`core-interfaces,core,persistance`) package with `-am`

## Raw evidence
- `c4-wave5-unit-tests-20260225-203312.txt`
- `c4-wave5-build-cache-20260225-203312.txt`
- `c4-wave5-build-web-gs-20260225-203312.txt`
- `c4-wave5-build-mp-stack-20260225-203312.txt`

## Decision
Wave 5 is complete and backward-compatible for runtime behavior. Cassandra metrics reporting is now exposed through a neutral snapshot contract, reducing another driver3-specific API surface before driver4 conversion waves.
