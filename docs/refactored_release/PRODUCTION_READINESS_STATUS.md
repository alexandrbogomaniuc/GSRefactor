# Production Readiness Status

`PRODUCTION_READY=NO`

## PR1. Runtime Gates Green Now

Status: `YES`

- migration guard is currently `PASS/PASS`
- healthcheck is currently `200`
- gameplay canary is currently `302 -> 200`

## PR2. Repo Truth

Status: `YES`

- branch: `cassandra-refactoring`
- repo must stay clean and synced before release actions continue

## PR3. Cassandra Production-Scale Risk

Status: `BLOCKED`

This is the only release blocker still open.

It can be closed by one of:

1. representative timing evidence at production-like scale, with per-table timings committed to the repo and linked to an archived evidence zip
2. explicit operator signoff to proceed without timing evidence, recorded with approver name, date, and rationale

Current state:

- local runtime parity is proven
- local archaeology is closed on this workstation
- no representative legacy Cassandra 3.11 dataset or read-only source access has been supplied yet

Required next input:

- use [LEGACY_SOURCE_STATS_AND_SNAPSHOT.md](LEGACY_SOURCE_STATS_AND_SNAPSHOT.md)
- or provide read-only legacy source access described in [PROD_MIGRATION_APPROVAL_REQUEST.md](PROD_MIGRATION_APPROVAL_REQUEST.md)

## PR4. Operator-Ready Runbook And Evidence Flow

Status: `YES`

Operator-facing materials are present:

- [RELEASE_CANDIDATE.md](RELEASE_CANDIDATE.md)
- [STAGING_REHEARSAL.md](STAGING_REHEARSAL.md)
- [MIGRATION_RUNBOOK.md](MIGRATION_RUNBOOK.md)
- [PROD_MIGRATION_APPROVAL_REQUEST.md](PROD_MIGRATION_APPROVAL_REQUEST.md)
- [LEGACY_SOURCE_STATS_AND_SNAPSHOT.md](LEGACY_SOURCE_STATS_AND_SNAPSHOT.md)
- [evidence/README_latest.md](evidence/README_latest.md)

Documented evidence paths already include:

- `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_093403/release_rehearsal_04.zip`
- `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_121415/prod_scale_proof_01.zip`

## PR5. Refactor Token Removal Is Production Acceptable

Status: `YES`

The safe local cleanup lane is exhausted. Remaining exceptions are intentional or runtime-sensitive and are not approved for blind cleanup:

- live wallet and host wiring in `gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- live endpoint wiring in `gs-server/game-server/web-gs/src/main/resources/ClusterConfiguration.xml`
- intentional compatibility support in `gs-server/sb-utils/src/main/java/com/dgphoenix/casino/common/util/ReflectionUtils.java`
- intentional smoke-fixture content under `gs-server/deploy/scripts`
- intentional compatibility fixtures and resource-coupled tests under the `gs-server/*/src/test` lanes

## Current Blockers

- Cassandra scale timing remains blocked on external operator input or deferral signoff

## Next Action

- wait for operators to provide the requested legacy source stats and snapshot pack, or a read-only legacy source access note, then run the production-scale timing rehearsal and update PR3
