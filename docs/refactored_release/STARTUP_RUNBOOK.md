# Refactored Release Startup Runbook

This runbook describes the release-candidate startup procedure that matches the latest green runtime proof.

## Scope

- Goal: bring up a playable GSRefactor stack on Cassandra 5.x without losing migration parity checks.
- Validation targets:
  - migration guard PASS/PASS
  - healthcheck `200`
  - gameplay canary `302 -> 200`

## Important Constraint

Docker Compose rejected the requested mixed-case project id `Refactored_versoin`. The validated runtime uses lowercase project id `refactored_versoin`. Keep the human-facing label `Refactored_versoin`, but use lowercase for the actual Compose invocation.

## Prerequisites

- Docker Desktop running
- Canonical checkout on `cassandra-refactoring`
- Maven available
- Runtime assets available under `runtime_smoke`
- Runtime-only Compose file present at:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/docker-compose.Refactored_versoin.yml`

## Build the Web Application

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server
mvn -s game-server/build/build-settings.xml -pl game-server/web-gs -am -DskipTests package
```

Expected result:

- `gs-server/game-server/web-gs/target/ROOT.war` is rebuilt successfully.

## Prepare Runtime Assets

The validated stack depends on:

- patched `ROOT.war`
- `export_localmachine` config bundle
- flattened html5 runtime assets under `runtime_smoke/out/fullstack_runtime/webapps/gs/ROOT/html5pc`

The currently proven automation that prepares these artifacts is the runtime-only fullstack smoke launcher:

```bash
bash /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/run_fullstack_smoke.sh
```

If you need a manual rebuild of html5 assets, the working smoke pipeline uses:

```bash
bash /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/deploy/scripts/refactor-bootstrap-runtime.sh
```

with runtime-smoke environment variables pointing at the legacy client and runtime output directories.

## Bring Up the Stack

Start infrastructure first:

```bash
docker compose -p refactored_versoin   -f /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/docker-compose.Refactored_versoin.yml   up -d cassandra_target cassandra_legacy zookeeper kafka webgs_static
```

Wait until:

- `cassandra-target` answers `cqlsh`
- `zookeeper-smoke` answers `ruok`
- `kafka-smoke` lists topics

Then start web-gs:

```bash
docker compose -p refactored_versoin   -f /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/docker-compose.Refactored_versoin.yml   up -d webgs
```

## Validation

### 1. Healthcheck

```bash
curl -i http://127.0.0.1:8080/support/health/check.jsp
```

Expected:

- `HTTP/1.1 200`

### 2. Gameplay Canary

First hop:

```bash
curl -i "http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en"
```

Expected:

- `HTTP/1.1 302`
- `Location:` header pointing to `/free/mp/template.jsp?...`

Follow the redirect:

```bash
curl -i "<Location header with :8080 restored if needed>"
```

Expected:

- `HTTP/1.1 200`

### 3. Migration Guard

```bash
bash /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/run_migration_smoke_loop.sh --once
cat /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/status/latest.env
```

Expected keys:

- `SCHEMA_OK=1`
- `ROWPROOF_OK=1`
- `ARCHIVER_LEGACY_OK=1`
- `ARCHIVER_TARGET_OK=1`

## Known Good Baseline

Fresh evidence used for this runbook:

- migration iteration: `iter_01_20260316_081816`
- fullstack iteration: `fullstack_20260316_082422`

## Failure Triage Order

1. Cassandra target not reachable
2. ZooKeeper/Kafka not ready
3. `webgs-smoke-fullstack` failed Spring startup
4. healthcheck returns non-`200`
5. gameplay entry returns non-`302` or follow-up template returns non-`200`
