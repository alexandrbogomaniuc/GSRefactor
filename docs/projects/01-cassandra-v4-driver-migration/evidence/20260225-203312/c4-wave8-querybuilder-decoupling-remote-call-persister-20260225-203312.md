# CASS-V4 Wave 8: QueryBuilder type decoupling in remote-call persister

Date (UTC): 2026-02-25 21:06
Project: `CASS-V4`

## Scope
Continue the query-construction decoupling track by removing direct querybuilder type imports from `CassandraRemoteCallPersister`.

## Implementation
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraRemoteCallPersister.java`

Changes:
- removed direct querybuilder type imports (`Insert`, `Select`).
- switched query variables to `Statement`.
- preserved existing query construction via inherited persister helpers (`getSelectColumnsQuery`, `getInsertQuery`, `eq`).

Touched code paths:
- `getRemoteCalls(int serverId)`
- `persist(PersistableCall entry)`

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
- `c4-wave8-unit-tests-20260225-203312.txt`
- `c4-wave8-build-cache-20260225-203312.txt`
- `c4-wave8-build-web-gs-20260225-203312.txt`
- `c4-wave8-build-mp-stack-20260225-203312.txt`

## Decision
Wave 8 is complete. This narrows another direct driver3 querybuilder dependency while retaining runtime behavior and existing persister query semantics.
