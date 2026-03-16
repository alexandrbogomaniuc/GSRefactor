# Refactored Release Startup Runbook

This runbook describes the release-candidate startup procedure that matches the latest green runtime proof.

## Scope

- Goal: bring up a playable GSRefactor stack on Cassandra 5.x without losing migration parity checks.
- Validation targets:
  - migration guard PASS/PASS
  - healthcheck `200`
  - gameplay canary `302 -> 200`

## Important Constraint

The currently proven baseline is not yet a single repo-tracked production Compose file. The authoritative green path today is the runtime-only harness:

- `run_migration_smoke_loop.sh --once` for migration proof
- `run_fullstack_smoke.sh` for playable fullstack boot

The older `refactored_versoin` consolidation remains useful reference, but it is not the current release-candidate source of truth after the latest smoke reruns.

## Prerequisites

- Docker Desktop running
- Canonical checkout on `cassandra-refactoring`
- Maven available
- Runtime assets available under `runtime_smoke`
- Runtime-only harness scripts present under:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/`

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

## Required Runtime Configuration

### Network aliases inside the fullstack runtime

`webgs-smoke-fullstack` must resolve these aliases:

- `fullstack-cassandra`
- `fullstack-zookeeper`
- `fullstack-kafka`

### Required JVM flags

The working runtime uses:

```text
-Dcassandra.hosts=fullstack-cassandra:9042
-Dzookeeper.connect=fullstack-zookeeper:2181
-Dzookeeper.hosts=fullstack-zookeeper:2181
-Dkafka.hosts=fullstack-kafka:9092
-Dkafka.bootstrap.servers=fullstack-kafka:9092
```

### Required configuration files and mounts

- patched WAR -> `/usr/local/tomcat/webapps/ROOT.war`
- export bundle -> `/www/html/gs/ROOT/export`
- html5 assets -> nginx `/usr/share/nginx/html/html5pc`
- application classpath properties in `WEB-INF/classes/`, especially:
  - `ClusterConfig.xml`
  - `SCClusterConfig.xml`
  - `BigStorageClusterConfig.xml`
  - `cluster-hosts.properties`
  - `settings.properties`

### Secret handling

- Do not commit operator credentials or environment-specific hostnames into the repo.
- Supply sensitive values through deployment-managed environment variables, mounted runtime config, or secret storage injected at container start.
- Treat the runtime-smoke bundles as local rehearsal assets only.

## Bring Up the Stack

The current release-candidate baseline is brought up by the runtime-only harness:

```bash
bash /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/run_fullstack_smoke.sh
```

The harness currently starts:

- Compose project `fullstacksmoke` for Cassandra, ZooKeeper, and Kafka
- standalone `webgs-static-fullstack`
- standalone `webgs-smoke-fullstack`

The latest proven run wrote:

- `COMPOSE_PROJECT=fullstacksmoke`
- `STATIC_EXTERNAL_PORT=18081`
- `WEBGS_CONTAINER_NAME=webgs-smoke-fullstack`

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
curl -i "<Location header>"
```

Expected:

- `HTTP/1.1 200`

If the direct `Location:` header omits `:8080` in a local smoke run, use the `GUEST_LAUNCH_URL` captured in the fullstack `summary.env` as the authoritative follow-up URL. The latest proven summary is:

- `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/fullstack_20260316_082422/summary.env`

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
- current static external port in the proven fullstack run: `18081`

## Failure Triage Order

1. Cassandra target not reachable
2. ZooKeeper/Kafka not ready
3. `webgs-smoke-fullstack` failed Spring startup
4. healthcheck returns non-`200`
5. gameplay entry returns non-`302` or follow-up template returns non-`200`
