# Release Candidate Manifest

## Candidate Status

- Track: Option A / production-track stabilization
- Runtime-proven application baseline commit: `d1d92c6820a409e01a2bb4037deab7c0313444a8`
- Branch: `cassandra-refactoring`
- Intended decision output: `GO` only if all checklist items below stay green

## GO / NO-GO Checklist

### Required services

- `fullstacksmoke-fullstack-cassandra-1`
- `fullstacksmoke-fullstack-zookeeper-1`
- `fullstacksmoke-fullstack-kafka-1`
- `webgs-static-fullstack`
- `webgs-smoke-fullstack`
- `cassandra-legacy` only while migration parity still needs proof or rollback confidence
- `cassandra-target` + `zookeeper-smoke` for the migration guard

### Startup order

1. `cassandra-legacy`
2. `cassandra-target`
3. `zookeeper-smoke`
4. `run_migration_smoke_loop.sh --once`
5. `run_fullstack_smoke.sh`
6. verify `webgs-static-fullstack` and `webgs-smoke-fullstack`

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
- followed template returns `200`
- authoritative smoke follow-up URLs live in `runtime_smoke/logs/fullstack_20260316_082422/summary.env`

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
- runtime-only patched WAR for smoke validation under `runtime_smoke/logs/fullstack_20260316_082422/ROOT.patched.war`

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
- `HTTP/1.1 302` for the canary entry and `200` for the follow-up template URL recorded by the smoke summary

## Evidence

- migration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260316_081816`
- fullstack: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/fullstack_20260316_082422`
- release docs: `docs/refactored_release/`

## Known Risks / Follow-Ups

- Remaining `com.datastax.driver.core` usage still exists in active modules; it is a tracked follow-up, not a release blocker for Option A.
- Rerunning the legacy smoke harness can recreate some live GSRefactor containers without compose labels, so docker cleanup must continue to classify by proven role plus script evidence, not labels alone.
- The current validated startup topology is still the hybrid runtime-smoke harness layout rather than a single tracked deploy asset; promoting a unified compose/deploy topology into the repo should be a separate review after release-candidate stabilization.
- `cqlsh COPY` is the proven migration path today, but it may need a throughput review before large production data moves.

## Rollback Trigger and Action

If migration verification fails or gameplay regresses after cutover:

1. stop or drain `webgs`
2. keep `cassandra-legacy` untouched as the source of truth
3. repoint runtime Cassandra connectivity back to the legacy node
4. bring `webgs` back up
5. re-run health and gameplay gates before restoring traffic
