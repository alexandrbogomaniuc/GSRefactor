# Phase 7 Cassandra Upgrade Target Rehearsal

- Timestamp (UTC): 2026-02-24T14:23:35Z
- Source container: gp3-c1-1
- Target container: refactor-c1-refactor-1
- Dry run: true

## Plan
1. Bootstrap target DB and copy keyspace schema + critical tables.
2. Run target preflight/schema/count/query evidence scripts.
3. Export source and target schema and generate schema diff.
4. Generate Phase 7 rehearsal report from latest manifest (target side).

Running bootstrap/copy step...
```text
DRY_RUN: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-target-bootstrap-and-critical-copy.sh --source-container gp3-c1-1 --target-container refactor-c1-refactor-1 --table-list /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt --output-dir /var/folders/7b/h207_tk50f5d2xyw6wxg0_mw0000gn/T/tmp.n8AzC1f9XH --wait-seconds 180
```

- Result: DRY_RUN_READY
- Next command (live): `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-target-bootstrap-and-critical-copy.sh --source-container gp3-c1-1 --target-container refactor-c1-refactor-1 --table-list /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt --output-dir /var/folders/7b/h207_tk50f5d2xyw6wxg0_mw0000gn/T/tmp.n8AzC1f9XH --wait-seconds 180`
