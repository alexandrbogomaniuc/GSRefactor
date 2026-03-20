# Rehearsal Template Option 1: Representative Snapshot

## Purpose

This template is for a representative data rehearsal using the existing documented migration path. It does not change the production migration mechanism.

## Inputs Required

- representative Cassandra 3.11 snapshot, SSTable tar, or equivalent export for:
  - `rcasinoks`
  - `rcasinoscks`
- target Cassandra 5.0.6 rehearsal environment
- repo-tracked release topology from `gs-server/deploy/refactored_release/`

## Execution Outline

1. restore or mount the representative legacy source into an isolated rehearsal environment
2. export authoritative schema from the legacy source
3. sanitize schema for Cassandra 5 compatibility
4. import sanitized schema into the Cassandra 5 target
5. run the current documented copy path on the selected large tables first
6. capture per-table timing:
   - start timestamp
   - end timestamp
   - rows copied
   - bytes copied
   - rows per second
   - MB per second
7. extrapolate a total duration estimate with assumptions clearly written down
8. re-run release verification gates:
   - migration guard expectations
   - health `200`
   - gameplay `302 -> 200`

## Required Outputs

- per-table timing summary
- total estimated duration
- docker compose config and service logs
- source size stats (`nodetool tablestats` or `cfstats`)
- evidence zip stored outside the repo

## Success Criteria

- timing is based on representative tables, not smoke-only tables
- the current migration path stays intact
- runtime gates remain green after the rehearsal

## Decision Rule

- if measured timing fits the allowed downtime window, keep the current migration runbook unchanged
- if measured timing does not fit, escalate to the DSBulk rehearsal template rather than changing the runbook silently
