# Latest Rehearsal Evidence Summary

## Canonical Truth

- branch: `cassandra-refactoring`
- baseline head under test: `2456db33ce58f8b34a03db069c3545f4014919b8`

## Evidence Timestamp

- summary generated: `2026-03-16 Europe/London`

## Migration Guard Snapshot

From `runtime_smoke/status/latest.env`:

```text
SCHEMA_OK=1
ROWPROOF_OK=1
ARCHIVER_LEGACY_OK=1
ARCHIVER_TARGET_OK=1
ITER_DIR=/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260316_081816
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
- the authoritative follow-up URL is the `GUEST_LAUNCH_URL` recorded by the fullstack summary file

## Compose Snapshot

Current playable baseline uses the hybrid harness topology:

```text
NAME                                   IMAGE                         COMMAND                  SERVICE               CREATED          STATUS          PORTS
fullstacksmoke-fullstack-cassandra-1   cassandra:5.0.6               "docker-entrypoint.s…"   fullstack-cassandra   recent           Up              7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp
fullstacksmoke-fullstack-kafka-1       confluentinc/cp-kafka:7.6.1   "/etc/confluent/dock…"   fullstack-kafka       recent           Up              9092/tcp
fullstacksmoke-fullstack-zookeeper-1   zookeeper:3.9                 "/docker-entrypoint.…"   fullstack-zookeeper   recent           Up              2181/tcp, 2888/tcp, 3888/tcp, 8080/tcp
```

Expected running services:

- `fullstacksmoke-fullstack-cassandra-1`
- `fullstacksmoke-fullstack-zookeeper-1`
- `fullstacksmoke-fullstack-kafka-1`

Standalone runtime containers on top of that baseline:

- `webgs-smoke-fullstack`
- `webgs-static-fullstack`

## Local Archived Evidence

- runtime-only bundle:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260316_144223/evidence_bundle`
- docker inventory archive:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260316_144223/docker_cleanup`

These paths are intentionally local and not release artifacts to publish from the repo.
