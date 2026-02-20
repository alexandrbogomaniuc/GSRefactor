# Phase 7 Cassandra Query Smoke Tooling (2026-02-20 16:55:12 UTC)

## Goal
Add repeatable query-compatibility and schema-diff tooling for Cassandra rehearsal evidence.

## Delivered
1. Query smoke script
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh`
- Executes `SELECT ... LIMIT N` across critical tables list and writes pass/fail summary log.

2. Schema diff script
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh`
- Compares source/target schema exports and emits diff artifact.

3. Supporting docs updates
- `/Users/alexb/Documents/Dev/Dev_new/docs/45-phase7-cassandra-query-compatibility-smoke-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/40-phase7-cassandra-upgrade-plan-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/41-phase7-cassandra-rehearsal-checklist-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/44-phase7-cassandra-schema-data-parity-template-v1.md`

## Verification
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh

/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh --help
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh --help

# schema-diff local smoke
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh --source /tmp/phase7-schema-a.cql --target /tmp/phase7-schema-b.cql
```

## Notes
- Query-smoke runtime execution still requires Docker socket access in target environment.
