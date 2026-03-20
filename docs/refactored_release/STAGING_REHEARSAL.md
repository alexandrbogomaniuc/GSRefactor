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
- runtime-only migration harness scripts under `runtime_smoke/bin/`
- repo-tracked deploy assets under `gs-server/deploy/refactored_release/`

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

### 3. Prepare the repo runtime bundle

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/deploy/refactored_release
cp -n .env.example .env
MP_LOBBY_WS_HOST=127.0.0.1:8080 \
STATIC_HTML5_SOURCE=/absolute/path/to/html5pc \
./prepare_runtime.sh
```

Expected:

- runtime bundle exists under `gs-server/deploy/refactored_release/runtime/`
- `.env` stays local and untracked

### 4. Repo compose rehearsal

```bash
cd /Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/deploy/refactored_release
docker compose -p refactored_release --env-file .env up -d --remove-orphans
docker compose -p refactored_release --env-file .env ps
```

Expected:

- `refactored_release-fullstack-cassandra-1`
- `refactored_release-fullstack-zookeeper-1`
- `refactored_release-fullstack-kafka-1`
- `refactored_release-webgs-static-1`
- `refactored_release-webgs-1`

### 5. Verification commands

```bash
curl -i http://127.0.0.1:8080/support/health/check.jsp
curl -i "http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en"
```

For the follow-up template proof, use the `Location:` header from the canary entry.
If the header omits `:8080`, rewrite it to `http://127.0.0.1:8080/...` before following it.

## Evidence Capture

Archive these artifacts for the rehearsal record:

- `docker compose -p refactored_release --env-file .env config`
- `docker compose -p refactored_release --env-file .env ps`
- `docker compose -p refactored_release --env-file .env logs`
- `docker ps -a`
- `runtime_smoke/status/latest.env`
- health curl output
- gameplay canary headers and follow-up curl output
- branch truth output (`HEAD`, `origin/cassandra-refactoring`, ahead/behind, clean status)

## Exit Criteria

The rehearsal is considered successful only if:

- migration guard is PASS/PASS
- health is `200`
- gameplay is `302 -> 200`
- evidence bundle is archived and shareable with release stakeholders
