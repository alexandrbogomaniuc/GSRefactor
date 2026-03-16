# Latest Rehearsal Evidence Summary

## Canonical Truth

- branch: `cassandra-refactoring`
- baseline head under test: `b4ce7aa725f472ee2dff92f4ed690b0241cdf3f4`

## Evidence Timestamp

- summary generated: `2026-03-16 Europe/London`

## Migration Guard Snapshot

From `runtime_smoke/status/latest.env`:

```text
SCHEMA_OK=1
ROWPROOF_OK=1
ARCHIVER_LEGACY_OK=1
ARCHIVER_TARGET_OK=1
ITER_DIR=/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260316_171634
```

## Fullstack Health

```text
curl -i http://127.0.0.1:8080/support/health/check.jsp
HTTP/1.1 200
```

## Gameplay Canary

```text
curl -i "http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en"
HTTP/1.1 302
follow-up template: HTTP/1.1 200
```

Current local nuance:

- the direct `Location:` header may omit `:8080` in local curl output
- rewrite it to `http://127.0.0.1:8080/...` before the follow-up curl

## Compose Snapshot

Current playable baseline uses the repo-tracked compose topology:

```text
NAME                                       IMAGE                         COMMAND                  SERVICE               STATUS
refactored_release-fullstack-cassandra-1   cassandra:5.0.6               "docker-entrypoint.s…"   fullstack-cassandra   Up (healthy)
refactored_release-fullstack-kafka-1       confluentinc/cp-kafka:7.6.1   "/etc/confluent/dock…"   fullstack-kafka       Up
refactored_release-fullstack-zookeeper-1   zookeeper:3.9                 "/docker-entrypoint.…"   fullstack-zookeeper   Up
refactored_release-webgs-1                 tomcat:9-jdk11                "catalina.sh run"        webgs                 Up
refactored_release-webgs-static-1          nginx:1.27-alpine             "/docker-entrypoint.…"   webgs-static          Up
```

Expected running services:

- `refactored_release-fullstack-cassandra-1`
- `refactored_release-fullstack-zookeeper-1`
- `refactored_release-fullstack-kafka-1`
- `refactored_release-webgs-1`
- `refactored_release-webgs-static-1`

## Local Archived Evidence

- runtime-only bundle:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260316_144223/evidence_bundle`
- docker inventory archive:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260316_144223/docker_cleanup`
- repo compose rehearsal logs:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/fullstack_20260316_171704`

These paths are intentionally local and not release artifacts to publish from the repo.
