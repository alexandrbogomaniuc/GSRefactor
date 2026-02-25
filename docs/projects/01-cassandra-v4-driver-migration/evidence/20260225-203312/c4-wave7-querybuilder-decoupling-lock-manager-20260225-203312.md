# CASS-V4 Wave 7: QueryBuilder decoupling in lock manager

Date (UTC): 2026-02-25 21:04
Project: `CASS-V4`

## Scope
Start reducing direct driver3 QueryBuilder coupling in high-risk persistence paths by converting `AbstractLockManager` to existing persister helper APIs.

## Implementation
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/AbstractLockManager.java`

Changes:
- removed direct `QueryBuilder` imports and calls.
- replaced direct calls with inherited helper APIs from `AbstractCassandraPersister`:
  - `getSelectColumnsQuery(...)`
  - `eq(...)`
  - `set(...)`
- converted select/query local variable types to `Statement` where appropriate.

Touched code paths:
- `getCurrentLocker(...)`
- `persist(...)`
- `delete(...)`
- `getLockIds(...)`

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
- `c4-wave7-unit-tests-20260225-203312.txt`
- `c4-wave7-build-cache-20260225-203312.txt`
- `c4-wave7-build-web-gs-20260225-203312.txt`
- `c4-wave7-build-mp-stack-20260225-203312.txt`

## Decision
Wave 7 is complete. This is a safe first conversion of query-construction usage in a hotspot class, reducing one more direct driver3 dependency without runtime behavior change.
