# Refactored Release Staging Rehearsal

This note captures the smallest repeatable rehearsal that matches the current Option A release path.

## Goal

Prove that we can:

1. rebuild the web artifact
2. replay the migration proof on a staging-grade snapshot
3. boot the fullstack rehearsal
4. verify health, gameplay entry, and migration parity
5. archive evidence for sign-off

## Rehearsal Inputs

- canonical branch: `cassandra-refactoring`
- legacy snapshot or equivalent data source for `cassandra-legacy`
- target Cassandra image: `cassandra:5.0.6`
- runtime-only harness scripts under `runtime_smoke/bin/`

## Rehearsal Steps

### 1. Build

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server
mvn -s game-server/build/build-settings.xml -pl game-server/web-gs,support/archiver -am test
```

Expected:

- `RC=0`

### 2. Migration rehearsal

Run one migration proof pass against the staging snapshot:

```bash
bash /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/run_migration_smoke_loop.sh --once
```

Expected in `runtime_smoke/status/latest.env`:

- `SCHEMA_OK=1`
- `ROWPROOF_OK=1`
- `ARCHIVER_LEGACY_OK=1`
- `ARCHIVER_TARGET_OK=1`

### 3. Fullstack rehearsal

```bash
bash /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/run_fullstack_smoke.sh
```

Expected:

- `FULLSTACK_RC=0`
- health `200`
- gameplay entry `302`
- template follow-up `200`

### 4. Verification commands

```bash
curl -i http://127.0.0.1:8080/support/health/check.jsp
curl -i "http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en"
```

For the follow-up template proof, prefer the URL recorded in the fullstack `summary.env`.

## Evidence Capture

Archive these artifacts for the rehearsal record:

- `runtime_smoke/status/latest.env`
- latest `runtime_smoke/logs/fullstack_*/summary.env`
- health curl output
- gameplay canary headers and follow-up curl output
- branch truth output (`HEAD`, `origin/cassandra-refactoring`, ahead/behind, clean status)

## Exit Criteria

The rehearsal is considered successful only if:

- migration guard is PASS/PASS
- health is `200`
- gameplay is `302 -> 200`
- evidence bundle is archived and shareable with release stakeholders
