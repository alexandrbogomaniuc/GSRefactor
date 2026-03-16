# Refactored Release Service Inventory

This directory captures the release-candidate topology that was re-validated on 2026-03-16 with:

- migration guard: `runtime_smoke/status/latest.env` updated from `iter_01_20260316_081816`
- fullstack smoke: `runtime_smoke/logs/fullstack_20260316_082422`
- health: `200` on `/support/health/check.jsp`
- gameplay canary: `302 -> 200` via `cwguestlogin.do`

The currently validated Docker Compose project id is `refactored_versoin`.

## Minimum Playable Runtime

| Service | Image | Required For | Dependency Order | Host Ports | Readiness Check | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| `cassandra-target` | `cassandra:5.0.6` | App runtime, migration target | 1 | `9043 -> 9042` | `cqlsh -e "SELECT release_version FROM system.local;"` | Runtime app traffic points here through alias `fullstack-cassandra`. |
| `zookeeper-smoke` | `zookeeper:3.9` | Kafka + app runtime | 2 | `2181 -> 2181` | `echo ruok \| nc -w 2 127.0.0.1 2181` | Required before Kafka and web-gs. |
| `kafka-smoke` | `confluentinc/cp-kafka:7.6.1` | App runtime | 3 | `9092 -> 9092` | `kafka-topics --bootstrap-server fullstack-kafka:9092 --list` | Advertised listener is `fullstack-kafka:9092`. |
| `webgs-static-fullstack` | `nginx:1.27-alpine` | Browser game assets | 4 | `18080 -> 80` | `GET /html5pc/actiongames/dragonstone/lobby/version.json` returns `200` | Serves legacy html5 assets for gameplay. |
| `webgs-smoke-fullstack` | `tomcat:9-jdk11` | Healthcheck + playable launch | 5 | `8080 -> 8080` | `GET /support/health/check.jsp` returns `200` | Mounts patched `ROOT.war` and export config bundle. |

## Migration Support Services

| Service | Image | Purpose | Host Ports | Notes |
| --- | --- | --- | --- | --- |
| `cassandra-legacy` | `cassandra:3.11` | Migration source of truth | `9042 -> 9042` | Keep this running while migration parity must remain provable. |

## Service Wiring

- `webgs-smoke-fullstack` talks to:
  - Cassandra via `fullstack-cassandra:9042`
  - ZooKeeper via `fullstack-zookeeper:2181`
  - Kafka via `fullstack-kafka:9092`
- `webgs-static-fullstack` is not on the request path for the healthcheck, but it is required for browser gameplay.
- `cassandra-legacy` is not required for the minimum playable runtime. It is required for the migration guard and rollback confidence.

## Launch/Verification Endpoints

- Health: `http://127.0.0.1:8080/support/health/check.jsp`
- Gameplay entry: `http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en`
- Expected follow-up template path: `/free/mp/template.jsp?...`

## Evidence Pointers

- Migration proof snapshot: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/status/latest.env`
- Fresh migration iteration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260316_081816`
- Fresh fullstack iteration: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/fullstack_20260316_082422`
