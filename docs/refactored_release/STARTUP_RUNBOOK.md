# Refactored Release Startup Runbook

This runbook describes the release-candidate startup procedure that matches the latest green runtime proof.

## Scope

- Goal: bring up a playable GSRefactor stack on Cassandra 5.x without losing migration parity checks.
- Validation targets:
  - migration guard PASS/PASS
  - healthcheck `200`
  - gameplay canary `302 -> 200`

## Important Constraint

The green release-candidate baseline is now split into two authoritative paths:

- repo-tracked compose under `gs-server/deploy/refactored_release/` for the playable web runtime on `:8080`
- `run_migration_smoke_loop.sh --once` for migration proof against legacy `3.11` and target `5.0.6`

This keeps production startup and migration rehearsal explicit without reopening broad refactor work.

## Prerequisites

- Docker Desktop running
- Canonical checkout on `cassandra-refactoring`
- Maven available
- Runtime assets available under `runtime_smoke`
- Runtime-only harness scripts present under:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/`
- Repo-tracked release template manifests under:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/deploy/refactored_release/`

## Build the Web Application

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server
mvn -s game-server/build/build-settings.xml -pl game-server/web-gs -am -DskipTests package
```

Expected result:

- `gs-server/game-server/web-gs/target/ROOT.war` is rebuilt successfully.

## Prepare Runtime Assets

The repo-tracked compose expects a prepared local runtime bundle:

- patched `ROOT.war`
- `export_localmachine` config bundle with `MP_LOBBY_WS_HOST` set for the chosen port
- optional `html5pc` asset path for the standalone static facade

Generate that bundle from the repo checkout:

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/deploy/refactored_release
MP_LOBBY_WS_HOST=127.0.0.1:8080 \
STATIC_HTML5_SOURCE=/absolute/path/to/html5pc \
./prepare_runtime.sh
```

Notes:

- `STATIC_HTML5_SOURCE` is optional for the healthcheck and `302 -> 200` gameplay canary.
- use a real `html5pc` bundle when you want the local static facade to serve legacy client assets during gameplay rehearsal.

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

Bring up the repo-tracked runtime:

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/deploy/refactored_release
cp -n .env.example .env
docker compose -p refactored_release --env-file .env up -d --remove-orphans
docker compose -p refactored_release --env-file .env ps
```

The current proven compose runtime starts:

- `refactored_release-fullstack-cassandra-1`
- `refactored_release-fullstack-zookeeper-1`
- `refactored_release-fullstack-kafka-1`
- `refactored_release-cassandra-init-1` as a one-shot keyspace/bootstrap gate
- `refactored_release-webgs-static-1`
- `refactored_release-webgs-1`

## Repo-Tracked Release Template

The repo-tracked deploy assets are now the authoritative playable runtime path:

- `gs-server/deploy/refactored_release/docker-compose.yml`
- `gs-server/deploy/refactored_release/.env.example`
- `gs-server/deploy/refactored_release/prepare_runtime.sh`
- `gs-server/deploy/refactored_release/nginx/default.conf`
- `gs-server/deploy/refactored_release/nginx/games.override.conf`

`runtime_smoke` remains authoritative for migration PASS/PASS evidence and local archival bundles.

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

If the direct `Location:` header omits `:8080` in a local smoke run, rewrite the host to `http://127.0.0.1:8080/...` before following it.

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

- migration iteration: `iter_01_20260316_171634`
- repo compose health/gameplay proof on `:8080`: `2026-03-16 17:31 Europe/London`
- auxiliary repo compose rehearsal on `:18088`: `2026-03-16 17:29 Europe/London`

## Failure Triage Order

1. Cassandra target not reachable
2. ZooKeeper/Kafka not ready
3. `webgs-smoke-fullstack` failed Spring startup
4. healthcheck returns non-`200`
5. gameplay entry returns non-`302` or follow-up template returns non-`200`
