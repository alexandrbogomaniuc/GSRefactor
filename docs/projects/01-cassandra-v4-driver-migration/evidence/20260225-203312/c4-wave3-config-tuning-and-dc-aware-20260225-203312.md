# CASS-V4 Wave 3: Configurable Driver Settings + Optional DC-aware Policy

Date (UTC): 2026-02-25 20:44
Project: `CASS-V4`

## Scope
Make Cassandra connection behavior configurable (instead of hardcoded) and add optional DC-aware routing policy to prepare safe migration toward Cassandra 4 and driver4 semantics.

## Implementation
### 1) Extended cluster configuration model
Added new config fields with backward-compatible defaults in:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/config/ClusterConfig.java`

New fields:
- `connectTimeoutMillis` (default `10000`)
- `readTimeoutMillis` (default `50000`)
- `tcpNoDelay` (default `true`)
- `reuseAddress` (default `true`)
- `keepAlive` (default `true`)
- `maxConnectionsPerHost` (default `2`)
- `coreConnectionsPerHost` (default `2`)
- `heartbeatIntervalSeconds` (default `90`)
- `maxRequestsPerConnection` (default `8192`)
- `enableDcAwareLoadBalancing` (default `false`)

### 2) Applied configurable values in cluster builder
Updated:
- `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/KeyspaceConfiguration.java`

Changes:
- socket and pooling values are now read from `ClusterConfig` getters.
- added `configureLoadBalancing(...)`:
  - if `enableDcAwareLoadBalancing=true` and `localDataCenterName` is set, applies `TokenAwarePolicy(DCAwareRoundRobinPolicy)`.
  - otherwise keeps existing default load-balancing behavior.

### 3) Test coverage updates
Updated test config and assertions:
- `gs-server/cassandra-cache/cache/src/test/resources/NetworkTopologyClusterConfig.xml`
- `gs-server/cassandra-cache/cache/src/test/java/com/dgphoenix/casino/cassandra/config/ClusterConfigDeserializationTest.java`

Validation now covers:
- default values for newly added fields,
- non-default parsed values,
- default-off and enabled cases for DC-aware load balancing.

## Validation
- Unit tests: PASS
  - `ClusterConfigDeserializationTest`
  - `KeyspaceConfigurationFactoryTest`
- Build: PASS
  - `gs-server/cassandra-cache/cache` package build

## Raw evidence
- `c4-wave3-unit-tests-20260225-203312.txt`
- `c4-wave3-build-cache-20260225-203312.txt`

## Decision
Wave 3 is complete and backward-compatible. Runtime behavior is unchanged unless new config flags are explicitly provided.
