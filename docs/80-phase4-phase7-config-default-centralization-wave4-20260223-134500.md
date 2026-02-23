# Phase 4 / Phase 7 Config Default Centralization (Wave 4)

## What was done
- Phase 4 protocol tooling defaults now read host-mode protocol/GS endpoints from `cluster-hosts.properties` via the shared helper:
  - `phase4-json-xml-parity-check.sh`
  - `phase4-runtime-readiness-check.sh`
  - `phase4-protocol-runtime-evidence-pack.sh`
  - `phase4-protocol-wallet-canary-probe.sh`
- Phase 7 Cassandra tooling defaults now read the refactor Cassandra container name from centralized config:
  - `phase7-cassandra-preflight.sh`
  - `phase7-cassandra-schema-export.sh`
  - `phase7-cassandra-table-counts.sh`
  - `phase7-cassandra-query-smoke.sh`
  - `phase7-cassandra-evidence-pack.sh`
- Added centralized config keys:
  - `PROTOCOL_ADAPTER_EXTERNAL_HOST`
  - `PROTOCOL_ADAPTER_EXTERNAL_PORT`
  - `CASSANDRA_EXTERNAL_HOST`
  - `CASSANDRA_EXTERNAL_PORT`
  - `CASSANDRA_REFACTOR_CONTAINER`
- Synced `cluster-hosts.properties` to portal-visible classpath copy.

## Why this matters (high level)
- Reduces hardcoded endpoint drift in Phase 4 runtime/evidence tooling.
- Makes Phase 7 Cassandra scripts use one configurable refactor container default instead of repeating `refactor-c1-1` across scripts.
- Preserves backward compatibility because all CLI flags still override defaults.

## Validation
- `sync-cluster-hosts.sh` passed.
- `bash -n` passed for modified Phase 4 and Phase 7 scripts.
- `--help` passed for representative modified scripts (Phase 4 runtime/readiness, Phase 7 evidence/preflight).
- `phase5-6-local-verification-suite.sh` regression check passed after config changes.
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-134216.md`
  - summary: PASS=18, FAIL=0, SKIP=0

## Scope note
- Phase 4 docker-only probe internals (loopback URLs inside `docker exec`) were intentionally left unchanged in this wave because they are container-local, not host-mode operator endpoints.
