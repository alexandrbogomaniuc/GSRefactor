# CASS-V4 Wave 6: IKeyspaceManager interface neutralization

Date (UTC): 2026-02-25 21:01
Project: `CASS-V4`

## Scope
Remove the remaining driver3-specific methods from the `IKeyspaceManager` interface surface so cross-module consumers no longer depend on driver `Session` and `Host` types.

## Implementation
### 1) Neutralized keyspace manager public interface
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/IKeyspaceManager.java`

Changes:
- removed `Session getSession()` from interface.
- removed `Set<Host> getDownHosts()` from interface.
- retained driver-neutral methods:
  - `getDownHostAddresses()`
  - `getAllHostAddresses()`
  - `getMetricsSnapshot()`

### 2) Preserved internal compatibility in implementation
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/KeyspaceManagerImpl.java`

Changes:
- kept existing `getSession()` and `getDownHosts()` methods as internal/legacy accessors (no longer part of interface contract).
- added method comments clarifying compatibility intent.

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
- `c4-wave6-unit-tests-20260225-203312.txt`
- `c4-wave6-build-cache-20260225-203312.txt`
- `c4-wave6-build-web-gs-20260225-203312.txt`
- `c4-wave6-build-mp-stack-20260225-203312.txt`

## Decision
Wave 6 is complete. `IKeyspaceManager` is now driver-neutral at the interface boundary, further reducing migration risk and dependency bleed before deeper driver4 query/persister conversion waves.
