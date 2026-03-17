# Latest Rehearsal Evidence Summary

## Canonical Truth

- branch: `cassandra-refactoring`
- baseline head under test: `adf1dc98597465eae8070d9d8f446432f2ccad03`

## Evidence Timestamp

- summary generated: `2026-03-17 Europe/London`

## Migration Guard Snapshot

From `runtime_smoke/status/latest.env`:

```text
SCHEMA_OK=1
ROWPROOF_OK=1
ARCHIVER_LEGACY_OK=1
ARCHIVER_TARGET_OK=1
ITER_DIR=/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260317_045853
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
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_050241/release_rehearsal_02.zip`
- docker inventory archive:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_050241/release_rehearsal_02/`
- repo compose rehearsal bundle contents:
  - `docker_compose_config.yml`
  - `docker_compose_ps.txt`
  - `docker_compose_logs.txt`
  - `docker_ps_a.txt`
  - `latest.env`
  - `health_headers.txt`
  - `canary_entry_headers.txt`
  - `canary_follow_headers.txt`

These paths are intentionally local and not release artifacts to publish from the repo.
