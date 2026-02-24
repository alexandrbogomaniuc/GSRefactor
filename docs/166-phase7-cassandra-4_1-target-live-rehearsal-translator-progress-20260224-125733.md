# Phase 7 Cassandra 4.1 Target Live Rehearsal Translator Progress

- Timestamp (UTC): 2026-02-24T12:57:33Z
- Scope: live upgrade rehearsal from legacy source gp3-c1-1 (Cassandra 2.1.20) to separate refactor target refactor-c1-refactor-1 (Cassandra 4.1.10)
- Goal: keep existing refactor-c1-1 unchanged while validating schema/data migration feasibility on a parallel target

## What Was Executed
1. Started c1-refactor target container from cassandra:4.1 (runtime reports 4.1.10).
2. Started legacy source container gp3-c1-1 and ran one-command Phase 7 target rehearsal repeatedly.
3. Patched translation/runtime tooling based on real 2.1 -> 4.1 failures.
4. Re-ran clean rehearsals until keyspace schema import and critical-table copy succeeded on target.

## Real Fixes Applied During Live Rehearsal
- cqlsh path compatibility for Cassandra 4.x (/opt/cassandra/bin/cqlsh) in Phase 7 helper/bootstrap paths.
- POSIX shell compatibility inside sh -lc checks ([ ... ] instead of [[ ... ]).
- Bootstrap script portability (mapfile removal for macOS bash 3.2).
- Schema sanitizer (2.1 -> 4.1):
  - remove dropped options dclocal_read_repair_chance, read_repair_chance
  - translate legacy caching values/map-string into valid CQL map literal with single quotes
  - remove CREATE KEYSPACE from imported schema (keyspace created explicitly)
- Critical table CSV import hardening: strip blank lines before COPY FROM (Cassandra 4.1 rejects empty rows).
- Phase 7 artifact correctness:
  - OUT_FILE now computed after arg parsing in preflight/schema/count/query scripts (prevents source/target output overwrite)
  - orchestrator now generates and uses a fresh evidence-pack manifest for the target run (instead of stale manifest)
  - orchestrator fail-fast on bootstrap/schema-export failures

## Latest Verified Live Rehearsal (Successful Translator Pass)
- Orchestrator report:
  - /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-upgrade-target-rehearsal-20260224-125539.md
- Bootstrap/copy report:
  - /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-target-bootstrap-and-critical-copy-20260224-125539.md
- Target evidence manifest:
  - /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-evidence-pack-20260224-125557.manifest.txt
- Target rehearsal report (template generated from fresh manifest):
  - /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-rehearsal-report-20260224-125602.md

## Result Snapshot (Target Bootstrap/Critical Copy)
- Keyspace schema import:
  - rcasinoks:OK
  - rcasinoscks:OK
- Critical table copy:
  - rcasinoscks.accountcf:OK:1
  - rcasinoscks.accountcf_ext:OK:1
  - rcasinoscks.frbonuscf:OK:4
  - rcasinoscks.paymenttransactioncf2:EMPTY
  - rcasinoks.gamesessioncf:OK:68
- Target validation scripts on refactor-c1-refactor-1:
  - preflight code=0
  - schema-export code=0
  - table-counts code=0
  - query-smoke code=0
- Bootstrap report result:
  - PARTIAL_SUCCESS_CRITICAL_TABLE_COPY_COMPLETE

## Critical Table Count Parity (Source vs Target)
- Source count report:
  - /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-table-counts-gp3-c1-1-20260224-125628.txt
- Target count report:
  - /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-table-counts-refactor-c1-refactor-1-20260224-125558.txt
- Observed parity for the rehearsal table set:
  - accountcf = 1 (match)
  - accountcf_ext = 1 (match)
  - frbonuscf = 4 (match)
  - paymenttransactioncf2 = 0 (match; source exports blank line only, now sanitized to EMPTY)
  - gamesessioncf = 68 (match)

## Remaining Gaps / Blockers
1. Full schema diff remains large (5605 lines) because DESCRIBE KEYSPACE output differs heavily across Cassandra 2.1 vs 4.1 (new default options, option formatting, ordering). Raw diff is not a meaningful parity verdict without normalization.
2. This rehearsal imports keyspaces and critical tables successfully, but full data migration for all tables/keyspaces is not complete yet.
3. GS/MP/other services have not yet been restarted against c1-refactor in this batch.

## Next Step
- Run a dedicated component test wave against c1-refactor (GS/MP + key flows) and add a normalized schema-compare step (or comparator rules) for 2.1 vs 4.1 option noise.
