# Phase 7 - Cassandra Evidence Pack Degraded Docker API Denied Statuses (2026-02-24)

## Scope
Harden Phase 7 Cassandra rehearsal tooling so Docker API socket permission failures are reported as explicit degraded status (`SKIP_DOCKER_API_DENIED`) instead of misleading PASS/FAIL results.

## What Changed
- Added shared Phase 7 Cassandra script helper:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/phase7-cassandra.sh`
  - wraps `docker exec ... cqlsh` and maps Docker API socket permission errors to exit code `3`
  - writes standardized stub outputs with `status=SKIP_DOCKER_API_DENIED`
- Updated Phase 7 Cassandra scripts to use the helper and emit degraded stub files when Docker API access is blocked:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-preflight.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-table-counts.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh`
- Updated Phase 7 orchestrator manifest handling:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-evidence-pack.sh`
  - exit code `3` now records `:SKIP:DOCKER_API_DENIED` in the manifest (instead of `FAIL` or accidental `PASS`)
- Fixed an implementation bug discovered during validation:
  - bash `if ! cmd; then code=$?` loses the original exit code
  - replaced with `set +e` / capture / `set -e` for correct classification

## Runtime Validation (This Environment)
Executed the real Phase 7 evidence pack against the refactor Cassandra container target:

```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-evidence-pack.sh --container refactor-c1-1
```

Observed environment limitation:
- Docker API socket access denied (`/Users/alexb/.docker/run/docker.sock`)

Result is now explicit and accurate:
- manifest records `SKIP_DOCKER_API_DENIED` for Docker-dependent steps
- stub outputs are generated for each skipped artifact with machine-readable status fields

## Evidence
- Manifest (degraded but valid):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-evidence-pack-20260224-080339.manifest.txt`
- Stub outputs with standardized status:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-preflight-20260224-080339.log`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-refactor-c1-1-20260224-080340.cql`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-table-counts-refactor-c1-1-20260224-080340.txt`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-query-smoke-refactor-c1-1-20260224-080340.log`

## Validation Performed
- Syntax checks:
  - `bash -n` on helper + modified Phase 7 scripts ✅
- Help checks:
  - `phase7-cassandra-preflight.sh --help` ✅
  - `phase7-cassandra-evidence-pack.sh --help` ✅
- Real orchestrator run:
  - `phase7-cassandra-evidence-pack.sh --container refactor-c1-1` ✅ (degraded manifest behavior verified)

## Impact
- Phase 7 evidence tooling is safer for sandboxed/restricted Docker sessions.
- Operators can distinguish:
  - real Cassandra query/schema failures
  - environment-level Docker API access blockers
- This improves report quality and reduces false interpretation during rehearsal preparation.

## Compatibility / Rollback
- No production runtime behavior change (tooling only).
- Rollback: revert this increment; Phase 7 scripts return to previous manifest behavior.
