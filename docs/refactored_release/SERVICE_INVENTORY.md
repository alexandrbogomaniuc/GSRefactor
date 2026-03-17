# Refactored Release Service Inventory

This directory captures the release-candidate topology that was re-validated on 2026-03-17 with:

- migration guard: `runtime_smoke/status/latest.env` updated from `iter_01_20260317_045853`
- repo compose rehearsal: `runtime_smoke/archive_20260317_050241/release_rehearsal_02.zip`
- health: `200` on `/support/health/check.jsp`
- gameplay canary: `302 -> 200` via `cwguestlogin.do`

The currently validated playable runtime is the repo-tracked Compose topology:

- Compose project: `refactored_release`
- playable services run as `refactored_release-*`
- migration proof remains separate and uses named containers `cassandra-legacy`, `cassandra-target`, and `zookeeper-smoke`
- older `fullstacksmoke-*` and standalone `webgs-*fullstack` containers are now local compatibility leftovers, not the production-track authoritative path

## Minimum Playable Runtime

| Service | Image | Required For | Dependency Order | Host Ports | Readiness Check | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| `refactored_release-fullstack-cassandra-1` | `cassandra:5.0.6` | App runtime | 1 | `9143 -> 9042` | `docker exec refactored_release-fullstack-cassandra-1 cqlsh -e "DESCRIBE KEYSPACES"` | Resolved inside the release network as `fullstack-cassandra`. |
| `refactored_release-fullstack-zookeeper-1` | `zookeeper:3.9` | Kafka + app runtime | 2 | `12181 -> 2181` | `docker exec refactored_release-fullstack-zookeeper-1 sh -c 'echo ruok | nc 127.0.0.1 2181'` | Resolved as `fullstack-zookeeper`. |
| `refactored_release-fullstack-kafka-1` | `confluentinc/cp-kafka:7.6.1` | App runtime | 3 | internal only | `docker exec refactored_release-fullstack-kafka-1 kafka-topics --bootstrap-server fullstack-kafka:9092 --list` | Resolved as `fullstack-kafka`. |
| `refactored_release-webgs-static-1` | `nginx:1.27-alpine` | Browser game assets | 4 | `18080 -> 80` | `GET /html5pc/actiongames/dragonstone/lobby/version.json` returns `200` | Serves legacy html5 assets from the repo-tracked runtime bundle. |
| `refactored_release-webgs-1` | `tomcat:9-jdk11` | Healthcheck + playable launch | 5 | `8080 -> 8080` | `GET /support/health/check.jsp` returns `200` | Mounts the patched repo-tracked `ROOT.war` and export config bundle. |

## Migration Support Services

| Service | Image | Purpose | Host Ports | Notes |
| --- | --- | --- | --- | --- |
| `cassandra-legacy` | `cassandra:3.11` | Migration source of truth | `9042 -> 9042` | Keep this running while migration parity must remain provable. |
| `cassandra-target` | `cassandra:5.0.6` | Migration target | `9043 -> 9042` | Used by the migration guard, separate from the fullstacksmoke Cassandra service. |
| `zookeeper-smoke` | `zookeeper:3.9` | Archiver/runtime support during migration proof | `2181 -> 2181` | Required by the migration harness. |

## Service Wiring

- `refactored_release-webgs-1` talks to:
  - Cassandra via `fullstack-cassandra:9042`
  - ZooKeeper via `fullstack-zookeeper:2181`
  - Kafka via `fullstack-kafka:9092`
- `refactored_release-cassandra-init-1` is a one-shot bootstrap helper that creates and normalizes the required keyspaces before `webgs` starts.
- `refactored_release-webgs-static-1` is not on the request path for the healthcheck, but it is required for browser gameplay.
- `cassandra-legacy` is not required for the minimum playable runtime. It is required for the migration guard and rollback confidence.
- older `fullstacksmoke-*`, `webgs-static-fullstack`, and `kafka-smoke` containers may still exist locally, but they are not part of the current minimum proven baseline.

## Launch/Verification Endpoints

- Health: `http://127.0.0.1:8080/support/health/check.jsp`
- Gameplay entry: `http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en`
- Proven follow-up template URL source: `runtime_smoke/archive_20260317_050241/release_rehearsal_02/canary_follow_status.txt`
- Static asset host used by the latest green baseline: `127.0.0.1:18080`

## Evidence Pointers

- Migration proof snapshot: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/status/latest.env`
- Fresh migration iteration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260317_045853`
- Fresh repo compose rehearsal bundle: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_050241/release_rehearsal_02.zip`
