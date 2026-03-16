# Refactored Release Service Inventory

This directory captures the release-candidate topology that was re-validated on 2026-03-16 with:

- migration guard: `runtime_smoke/status/latest.env` updated from `iter_01_20260316_081816`
- fullstack smoke: `runtime_smoke/logs/fullstack_20260316_082422`
- health: `200` on `/support/health/check.jsp`
- gameplay canary: `302 -> 200` via `cwguestlogin.do`

The currently validated runtime is the March 16 hybrid harness topology:

- infra for fullstack boot comes from Compose project `fullstacksmoke`
- `webgs-smoke-fullstack` and `webgs-static-fullstack` are started as standalone named containers by the fullstack harness
- migration proof uses separate named containers for `cassandra-legacy`, `cassandra-target`, and `zookeeper-smoke`

The earlier `refactored_versoin` consolidation is still useful reference material, but it is not the current authoritative green baseline after the latest smoke reruns.

## Minimum Playable Runtime

| Service | Image | Required For | Dependency Order | Host Ports | Readiness Check | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| `fullstacksmoke-fullstack-cassandra-1` | `cassandra:5.0.6` | App runtime | 1 | internal only | `docker exec fullstacksmoke-fullstack-cassandra-1 cqlsh -e "SELECT release_version FROM system.local;"` | Resolved inside the harness network as `fullstack-cassandra`. |
| `fullstacksmoke-fullstack-zookeeper-1` | `zookeeper:3.9` | Kafka + app runtime | 2 | internal only | `docker exec fullstacksmoke-fullstack-zookeeper-1 sh -c 'echo ruok | nc 127.0.0.1 2181'` | Resolved as `fullstack-zookeeper`. |
| `fullstacksmoke-fullstack-kafka-1` | `confluentinc/cp-kafka:7.6.1` | App runtime | 3 | internal only | `docker exec fullstacksmoke-fullstack-kafka-1 kafka-topics --bootstrap-server fullstack-kafka:9092 --list` | Resolved as `fullstack-kafka`. |
| `webgs-static-fullstack` | `nginx:1.27-alpine` | Browser game assets | 4 | `18081 -> 80` | `GET /html5pc/actiongames/dragonstone/lobby/version.json` returns `200` | Serves legacy html5 assets for gameplay. |
| `webgs-smoke-fullstack` | `tomcat:9-jdk11` | Healthcheck + playable launch | 5 | `8080 -> 8080` | `GET /support/health/check.jsp` returns `200` | Mounts patched `ROOT.war` and export config bundle. |

## Migration Support Services

| Service | Image | Purpose | Host Ports | Notes |
| --- | --- | --- | --- | --- |
| `cassandra-legacy` | `cassandra:3.11` | Migration source of truth | `9042 -> 9042` | Keep this running while migration parity must remain provable. |
| `cassandra-target` | `cassandra:5.0.6` | Migration target | `9043 -> 9042` | Used by the migration guard, separate from the fullstacksmoke Cassandra service. |
| `zookeeper-smoke` | `zookeeper:3.9` | Archiver/runtime support during migration proof | `2181 -> 2181` | Required by the migration harness. |

## Service Wiring

- `webgs-smoke-fullstack` talks to:
  - Cassandra via `fullstack-cassandra:9042`
  - ZooKeeper via `fullstack-zookeeper:2181`
  - Kafka via `fullstack-kafka:9092`
- `webgs-static-fullstack` is not on the request path for the healthcheck, but it is required for browser gameplay.
- `cassandra-legacy` is not required for the minimum playable runtime. It is required for the migration guard and rollback confidence.
- `kafka-smoke` may still exist from older consolidation work, but it is not part of the current minimum proven baseline.

## Launch/Verification Endpoints

- Health: `http://127.0.0.1:8080/support/health/check.jsp`
- Gameplay entry: `http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en`
- Proven follow-up template URL source: `runtime_smoke/logs/fullstack_20260316_082422/summary.env`
- Static asset host currently used by the green baseline: `127.0.0.1:18081`

## Evidence Pointers

- Migration proof snapshot: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/status/latest.env`
- Fresh migration iteration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260316_081816`
- Fresh fullstack iteration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/fullstack_20260316_082422`
