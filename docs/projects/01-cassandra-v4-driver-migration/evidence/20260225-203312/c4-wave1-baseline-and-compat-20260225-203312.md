# CASS-V4 Wave 1: Baseline Inventory + Connection Compatibility

Date (UTC): 2026-02-25 20:33-20:35
Project: `CASS-V4`

## Scope of this wave
1. Add a Cassandra 4 migration-safe connection toggle for cluster-name validation.
2. Upgrade Phase 7 driver inventory tooling to be dynamic (no hardcoded local paths) and richer for migration planning.
3. Generate fresh inventory evidence and validate Cassandra cache module tests/build.

## Changes implemented
### 1) Runtime compatibility toggle
- Added `validateClusterName` to Cassandra cluster config model with backward-compatible default.
- Behavior:
  - If `validateClusterName=true` and `clusterName` is set: existing strict cluster-name validation remains active.
  - If `validateClusterName=false`: connection skips strict cluster-name validation (useful during mixed/topology migrations and Cassandra 4 transition where strict-name pinning can be a blocker).
- Files:
  - `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/config/ClusterConfig.java`
  - `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/KeyspaceConfiguration.java`

### 2) Test coverage updates
- Added explicit config coverage for both default and disabled modes.
- Files:
  - `gs-server/cassandra-cache/cache/src/test/resources/NetworkTopologyClusterConfig.xml`
  - `gs-server/cassandra-cache/cache/src/test/java/com/dgphoenix/casino/cassandra/config/ClusterConfigDeserializationTest.java`

### 3) Driver inventory tooling (Phase 7)
- Reworked `phase7-cassandra-driver-inventory.sh`:
  - dynamic root detection from script location/repo root,
  - optional CLI overrides (`--repo-root`, `--gs-root`, `--mp-root`, `--new-games-root`, `--out-dir`),
  - richer output sections:
    - dependency declarations,
    - driver3/driver4 import line counts,
    - top files by driver3 imports,
    - driver API type usage distribution.
- File:
  - `gs-server/deploy/scripts/phase7-cassandra-driver-inventory.sh`

## Validation results
- Unit tests: PASS
  - `ClusterConfigDeserializationTest`
  - `KeyspaceConfigurationFactoryTest`
- Build: PASS
  - `gs-server/cassandra-cache/cache` package build
- Script validation: PASS
  - `bash -n` syntax check
  - inventory script run with dynamic repo root/out-dir

## Key inventory findings (fresh baseline)
From `phase7-cassandra-driver-inventory-20260225-203429.txt`:
- GS codebase:
  - `driver3_import_lines=488`
  - `driver4_import_lines=0`
- MP codebase:
  - `driver3_import_lines=151`
  - `driver4_import_lines=0`
- New Games codebase:
  - `driver3_import_lines=0`
  - `driver4_import_lines=0`
- Dependency declarations still on driver 3 line (`cassandra-driver-core`, `cassandra-driver-mapping`) in GS/MP modules.

## Raw evidence files
- `phase7-cassandra-driver-inventory-20260225-203429.txt`
- `c4-wave1-unit-tests-20260225-203312.txt`
- `c4-wave1-build-cache-20260225-203312.txt`

## Decision after wave
Wave 1 is complete and tested. The migration is still in preparation/implementation stage (not cutover-ready yet). Next wave should start direct code migration of high-impact modules using this inventory baseline.
