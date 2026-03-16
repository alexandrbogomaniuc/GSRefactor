# Release Candidate Manifest

## Candidate Status

- Track: Option A / production-track stabilization
- Candidate baseline commit: `5ca5b9023e1ad9bf0c6bde265acc356706451b6d`
- Branch: `cassandra-refactoring`
- Intended decision output: `GO` only if all checklist items below stay green

## GO / NO-GO Checklist

### Required services

- `cassandra-target`
- `zookeeper-smoke`
- `kafka-smoke`
- `webgs-static-fullstack`
- `webgs-smoke-fullstack`
- `cassandra-legacy` only while migration parity still needs proof or rollback confidence

### Startup order

1. `cassandra-target`
2. `cassandra-legacy`
3. `zookeeper-smoke`
4. `kafka-smoke`
5. `webgs-static-fullstack`
6. `webgs-smoke-fullstack`

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

## Evidence

- migration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260316_081816`
- fullstack: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/fullstack_20260316_082422`
- release docs: `docs/refactored_release/`

## Known Risks / Follow-Ups

- Remaining `com.datastax.driver.core` usage still exists in active modules; it is a tracked follow-up, not a release blocker for Option A.
- Rerunning the legacy smoke harness can recreate some live GSRefactor containers without compose labels, so docker cleanup must continue to classify by proven role plus script evidence, not labels alone.
- The current validated startup topology is documented in repo but the actual compose file remains runtime-only under `runtime_smoke/`; promoting it into tracked deploy assets should be a separate review after release-candidate stabilization.
- `cqlsh COPY` is the proven migration path today, but it may need a throughput review before large production data moves.
