# Release Candidate Manifest

## Candidate Status

- Track: Option A / production-track stabilization
- Runtime-proven application baseline commit: `adf1dc98597465eae8070d9d8f446432f2ccad03`
- Branch: `cassandra-refactoring`
- Intended decision output: `GO` only if all checklist items below stay green

## GO / NO-GO Checklist

### Required services

- `refactored_release-fullstack-cassandra-1`
- `refactored_release-fullstack-zookeeper-1`
- `refactored_release-fullstack-kafka-1`
- `refactored_release-cassandra-init-1` during startup only
- `refactored_release-webgs-static-1`
- `refactored_release-webgs-1`
- `cassandra-legacy` only while migration parity still needs proof or rollback confidence
- `cassandra-target` + `zookeeper-smoke` for the migration guard

### Startup order

1. `cassandra-legacy`
2. `cassandra-target`
3. `zookeeper-smoke`
4. `run_migration_smoke_loop.sh --once`
5. `prepare_runtime.sh`
6. `docker compose -p refactored_release --env-file .env up -d --remove-orphans`
7. verify `refactored_release-webgs-static-1` and `refactored_release-webgs-1`

### Required runtime config

- aliases: `fullstack-cassandra`, `fullstack-zookeeper`, `fullstack-kafka`
- JVM flags:
  - `-Dcassandra.hosts=fullstack-cassandra:9042`
  - `-Dzookeeper.connect=fullstack-zookeeper:2181`
  - `-Dzookeeper.hosts=fullstack-zookeeper:2181`
  - `-Dkafka.hosts=fullstack-kafka:9092`
  - `-Dkafka.bootstrap.servers=fullstack-kafka:9092`

### Verification gates

- migration guard PASS/PASS in `runtime_smoke/status/latest.env`
- `curl -i http://127.0.0.1:8080/support/health/check.jsp` returns `200`
- `curl -i "http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en"` returns `302`
- followed template returns `200` after rewriting `http://127.0.0.1/...` to `http://127.0.0.1:8080/...` if needed
- `WEB_SOCKET_URL` in the launch URL resolves to `ws://127.0.0.1:8080/websocket/mplobby?...`

### Operational time buckets

- schema export + sanitize + import: minutes
- row copy with `cqlsh COPY`: minutes to hours depending on dataset size
- application boot + smoke verification: minutes

These are planning buckets for release rehearsal, not performance guarantees.

### Migration / rollback

- authoritative schema export source: legacy `DESCRIBE KEYSPACE`
- target import: Cassandra 5 compatible sanitized CQL
- copy strategy: `cqlsh COPY`
- rollback: repoint app back to legacy Cassandra, keep legacy read-only, do not rewrite legacy from target

## Build Commands

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server
mvn -s game-server/build/build-settings.xml -pl game-server/web-gs -am -DskipTests package
mvn -s game-server/build/build-settings.xml -pl game-server/web-gs,support/archiver -am test
```

Expected:

- both commands exit with `RC=0`

## Produced Artifacts

- `gs-server/game-server/web-gs/target/ROOT.war`
- `gs-server/support/archiver/target/casino-archiver.jar`
- repo-tracked prepared runtime bundle under `gs-server/deploy/refactored_release/runtime/`
- repo-tracked release manifests under `gs-server/deploy/refactored_release/`

## Docker Images Used In Smoke

- `cassandra:3.11`
- `cassandra:5.0.6`
- `zookeeper:3.9`
- `confluentinc/cp-kafka:7.6.1`
- `tomcat:9-jdk11`
- `nginx:1.27-alpine`

## Required Config Files and Secret Handling

Required runtime inputs for the proven rehearsal path:

- export bundle under `/www/html/gs/ROOT/export`
- classpath config under `WEB-INF/classes/`
  - `ClusterConfig.xml`
  - `SCClusterConfig.xml`
  - `BigStorageClusterConfig.xml`
  - `cluster-hosts.properties`
  - `settings.properties`

Secret handling rules:

- do not commit credentials or environment-specific endpoints into the repository
- inject sensitive values at deploy time through mounted config, deployment-managed environment variables, or secret storage
- treat runtime-smoke generated assets as rehearsal-only evidence, not release artifacts to commit
- keep `.env` local only; it is operator input and must not carry secrets in git

## Runtime Gate Commands

```bash
cat /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/status/latest.env
curl -i http://127.0.0.1:8080/support/health/check.jsp
curl -i "http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en"
```

Expected:

- `SCHEMA_OK=1`
- `ROWPROOF_OK=1`
- `ARCHIVER_LEGACY_OK=1`
- `ARCHIVER_TARGET_OK=1`
- `HTTP/1.1 200` for health
- `HTTP/1.1 302` for the canary entry and `200` for the follow-up template URL after local port rewrite if the header omits `:8080`

## Evidence

- migration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260317_045853`
- repo compose runtime: live `:8080` rehearsal from `2026-03-17 05:02 Europe/London`
- release docs: `docs/refactored_release/`
- latest summary: `docs/refactored_release/evidence/README_latest.md`

## Known Risks / Follow-Ups

- Remaining `com.datastax.driver.core` usage still exists in active modules; it is a tracked follow-up, not a release blocker for Option A.
- Rerunning the legacy smoke harness can recreate some live GSRefactor containers without compose labels, so docker cleanup must continue to classify by proven role plus script evidence, not labels alone.
- The repo compose path still expects an operator-provided html5 asset bundle if local gameplay needs the standalone static facade beyond the `302 -> 200` canary.
- `cqlsh COPY` is the proven migration path today, but it may need a throughput review before large production data moves.

## Rollback Trigger and Action

If migration verification fails or gameplay regresses after cutover:

1. stop or drain `webgs`
2. keep `cassandra-legacy` untouched as the source of truth
3. repoint runtime Cassandra connectivity back to the legacy node
4. bring `webgs` back up
5. re-run health and gameplay gates before restoring traffic
