# CASS-V4 Wave 4: Driver-neutral diagnosis host APIs

Date (UTC): 2026-02-25 20:51
Project: `CASS-V4`

## Scope
Reduce direct Cassandra driver3 API coupling in GS diagnosis/health code by exposing driver-neutral host-address APIs from the keyspace manager and switching diagnosis tasks to those APIs.

## Implementation
### 1) Extended keyspace manager interface with driver-neutral host APIs
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/IKeyspaceManager.java`

Added:
- `Set<String> getDownHostAddresses()`
- `Set<String> getAllHostAddresses()`

### 2) Implemented host-address APIs in keyspace manager
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/KeyspaceManagerImpl.java`

Changes:
- Added host extraction helper that safely resolves host name/IP from driver host socket data.
- Implemented down-host and all-host address getters with null/initialization guards.
- Preserved existing `getDownHosts()` behavior for backward compatibility.

### 3) Added test coverage for new APIs
Updated:
- `gs-server/cassandra-cache/cache/src/test/java/com/dgphoenix/casino/cassandra/KeySpaceManagerTest.java`

Added tests:
- `testDownHostAddresses`
- `testAllHostAddresses`

### 4) Switched diagnosis tasks to driver-neutral APIs
Updated:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/tasks/CassandraNodesCheckTask.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/SystemDiagnosisServlet.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/tasks/CassandraStateCheckTask.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/tasks/CassandraStateMonitoringTask.java`

Changes:
- Diagnosis code now uses `getDownHostAddresses()` and `getAllHostAddresses()`.
- Removed direct traversal of `Session -> Cluster -> Metadata -> Host` in diagnosis fallback paths.
- Diagnosis output now reports host address strings directly.

## Validation
- Unit tests: PASS
  - `KeySpaceManagerTest`
  - `ClusterConfigDeserializationTest`
  - `KeyspaceConfigurationFactoryTest`
- Build/package: PASS
  - `gs-server/cassandra-cache/cache` install
  - `gs-server/game-server/web-gs` package (with local cluster properties)

## Raw evidence
- `c4-wave4-unit-tests-20260225-203312.txt`
- `c4-wave4-build-web-gs-20260225-203312.txt`

## Decision
Wave 4 is complete and backward-compatible. Diagnosis paths are less coupled to driver3 internals, which reduces migration risk for future Java driver4 transition waves.
