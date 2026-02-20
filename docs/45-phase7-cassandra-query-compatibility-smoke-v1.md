# Phase 7 Cassandra Query Compatibility Smoke v1

Last updated: 2026-02-20 UTC

## Goal
Quickly validate that critical GS tables are readable on target Cassandra candidate using representative SELECT queries.

## Inputs
- Critical table list:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt`
- Cassandra container:
  - default `refactor-c1-1` (override per environment).

## Commands
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh \
  --container refactor-c1-1 \
  --table-list /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt \
  --limit 1
```

## Output
- Writes log to `docs/phase7/cassandra/phase7-cassandra-query-smoke-*.log`
- Exit codes:
  - `0` all queries pass,
  - `2` at least one table query failed.

## Pair with schema diff
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh \
  --source docs/phase7/cassandra/source-schema.cql \
  --target docs/phase7/cassandra/target-schema.cql
```

## Full evidence + report flow
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-evidence-pack.sh \
  --container refactor-c1-1

/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-rehearsal-report-generate.sh \
  --manifest docs/phase7/cassandra/phase7-cassandra-evidence-pack-<timestamp>.manifest.txt
```
