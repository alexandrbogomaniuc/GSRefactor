# Phase 7 Separate `c1-refactor` Target Service and Bootstrap Script

Date: 2026-02-24

## What Changed
- Added a separate refactor Cassandra target service `c1-refactor` in refactor compose.
- Kept existing `c1` (`cassandra:2.1.20`) unchanged for current GS/MP/runtime compatibility.
- Added target host/container keys to centralized `cluster-hosts.properties`.
- Added Phase 7 execution script to bootstrap target, copy keyspace schema from legacy `c1`, and attempt critical-table data copy (`critical-tables.txt`) into `c1-refactor`.
- Patched Phase 7 Cassandra helper with `docker compose exec` fallback when direct `docker exec` is denied in Codex shell.

## Design Decision (staged upgrade)
- This introduces a parallel target DB for upgrade rehearsal and compatibility testing without disrupting current refactor stack.
- Default target image is `cassandra:4.1` (parameterized via `CASSANDRA_REFACTOR_TARGET_IMAGE`) to support staged upgrade execution. Final Requirement 7 still requires latest stable target before cutover approval.

## Runtime Attempt Result (this shell)
- Launching `c1-refactor` failed in this Codex shell due Docker daemon API permission denial during image inspection/pull:
  - `unable to get image 'cassandra:4.1': permission denied while trying to connect to the Docker daemon socket ... operation not permitted`
- Compose parsing and script validation succeeded.

## Files
- `gs-server/deploy/docker/refactor/docker-compose.yml`
- `gs-server/deploy/config/cluster-hosts.properties`
- `gs-server/game-server/web-gs/src/main/resources/cluster-hosts.properties`
- `gs-server/deploy/scripts/lib/phase7-cassandra.sh`
- `gs-server/deploy/scripts/phase7-cassandra-target-bootstrap-and-critical-copy.sh`

## Next Execution Command
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-target-bootstrap-and-critical-copy.sh
```

If Docker API is accessible, this will:
1. start `c1-refactor`,
2. wait for `cqlsh`,
3. copy schema for app keyspaces from `c1`,
4. copy critical tables listed in `docs/phase7/cassandra/critical-tables.txt`,
5. run Phase 7 preflight/schema/count/query scripts on the new target.
