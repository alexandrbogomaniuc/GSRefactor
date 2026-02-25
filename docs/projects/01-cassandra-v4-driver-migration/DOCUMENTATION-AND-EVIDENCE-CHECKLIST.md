# CASS-V4 Documentation And Evidence Checklist

Last updated: 2026-02-25 UTC

## Required documents
1. Updated driver compatibility matrix.
2. Updated migration runbook with exact commands.
3. Schema parity report.
4. Data parity report.
5. Runtime regression summary.
6. Performance and resilience report.
7. Rollback drill report.
8. Final sign-off summary.

## Existing references to update
- `/Users/alexb/Documents/Dev/Dev_new/docs/40-phase7-cassandra-upgrade-plan-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/41-phase7-cassandra-rehearsal-checklist-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/42-phase7-cassandra-cutover-rollback-runbook-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/43-phase7-cassandra-driver-compatibility-matrix-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/44-phase7-cassandra-schema-data-parity-template-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/45-phase7-cassandra-query-compatibility-smoke-v1.md`

## Evidence file naming standard
- `c4-<area>-<YYYYMMDD-HHMMSS>.md`
- `c4-<area>-<YYYYMMDD-HHMMSS>.log`
- `c4-<area>-<YYYYMMDD-HHMMSS>.txt`

## Minimum evidence set before sign-off
- Driver inventory confirming 4.x adoption.
- Full parity evidence (schema + row counts + critical tables).
- Runtime end-to-end evidence on Cassandra 4 target.
- Full verification suite report after migration.
- Rollback drill evidence.

## Review checklist
- Plain-English summary exists for each evidence file.
- Every conclusion links to at least one raw proof file.
- Any failures are documented with status (`open`, `fixed`, `accepted risk`).

## Storage location
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/01-cassandra-v4-driver-migration/evidence`
