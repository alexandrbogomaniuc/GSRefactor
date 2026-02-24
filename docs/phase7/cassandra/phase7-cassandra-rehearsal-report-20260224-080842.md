# Cassandra Rehearsal Report Template

Date (UTC): 2026-02-24 08:08:42 UTC
Environment: refactor
Source cluster: legacy/refactor-c1-1 (set actual)
Target cluster: target-candidate (set actual)
Owner:

## Scope
- Keyspaces:
- Bank sample set:
- Feature flows:

## Executed steps
1.
2.
3.

## Evidence files
- Schema source: permission denied while trying to connect to the docker API at unix:///Users/alexb/.docker/run/docker.sock | schema_export=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-refactor-c1-1-20260224-080340.cql
- Schema target:
- Schema diff:
- Count report source: permission denied while trying to connect to the docker API at unix:///Users/alexb/.docker/run/docker.sock | table_counts=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-table-counts-refactor-c1-1-20260224-080340.txt
- Count report target:
- Runtime parity report: permission denied while trying to connect to the docker API at unix:///Users/alexb/.docker/run/docker.sock | query_smoke_log=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-query-smoke-refactor-c1-1-20260224-080340.log

## Results
- Schema parity: BLOCKED (Docker API denied during schema export)
- Data parity: BLOCKED (Docker API denied during table counts)
- Runtime parity: BLOCKED (Docker API denied during query smoke)
- Performance summary: BLOCKED (Docker API denied during preflight)

## Issues / risks
1.
2.

## Recommendation
- Go / No-Go: No-Go (blocked by Docker API permission denied in refactor rehearsal tooling run)
- Required follow-ups:
