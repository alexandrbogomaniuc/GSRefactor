# Phase 7 Cassandra Upgrade Target Rehearsal

- Timestamp (UTC): 2026-02-24T12:55:39Z
- Source container: gp3-c1-1
- Target container: refactor-c1-refactor-1
- Dry run: false

## Plan
1. Bootstrap target DB and copy keyspace schema + critical tables.
2. Run target preflight/schema/count/query evidence scripts.
3. Export source and target schema and generate schema diff.
4. Generate Phase 7 rehearsal report from latest manifest (target side).

Running bootstrap/copy step...
```text
Starting target service c1-refactor...
time="2026-02-24T12:55:39Z" level=warning msg="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
 Container refactor-c1-refactor-1 Running 
report=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-target-bootstrap-and-critical-copy-20260224-125539.md
artifact_dir=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/target-bootstrap-20260224-125539
```

## Schema Compare
- Source schema: `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-gp3-c1-1-20260224-125601.cql`
- Target schema: `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-refactor-c1-refactor-1-20260224-125602.cql`
```text
schema_diff=FOUND
schema_diff_file=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-diff-20260224-125602.patch
```

## Rehearsal Report Generator
- Manifest: `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-evidence-pack-20260224-125557.manifest.txt`
```text
running=preflight
preflight_log=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-preflight-20260224-125557.log
running=driver_inventory
driver_inventory=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-driver-inventory-20260224-125558.txt
running=schema_export
schema_export=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-refactor-c1-refactor-1-20260224-125558.cql
running=table_counts
table_counts=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-table-counts-refactor-c1-refactor-1-20260224-125558.txt
running=query_smoke
query_smoke_log=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-query-smoke-refactor-c1-refactor-1-20260224-125600.log
manifest=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-evidence-pack-20260224-125557.manifest.txt
```

```text
rehearsal_report=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-rehearsal-report-20260224-125602.md
```

- Result: REVIEW_GENERATED_ARTIFACTS
