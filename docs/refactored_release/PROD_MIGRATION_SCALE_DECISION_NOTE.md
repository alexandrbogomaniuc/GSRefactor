# Production Migration Scale Decision Note

## Status

- runtime parity is currently proven on the local release-track baseline:
  - migration guard `PASS/PASS`
  - healthcheck `200`
  - gameplay canary `302 -> 200`
- the runtime proof referenced here was captured while the canonical application code was clean and synced at `cb00cf441ff9d689c32afd0f726d1111e6a1cf65`

## What Is Proven

- the repo-tracked `refactored_release` startup topology can serve the health endpoint and the multiplayer launch canary
- the migration smoke path still proves:
  - `SCHEMA_OK=1`
  - `ROWPROOF_OK=1`
  - `ARCHIVER_LEGACY_OK=1`
  - `ARCHIVER_TARGET_OK=1`
- the current release-track runbook is sufficient for smoke-scale schema export, sanitize, import, and validation

## What Is Not Proven

- production-scale Cassandra migration duration is not proven
- there is no honest local basis to choose `cqlsh COPY` versus `DSBulk` for production-volume tables
- no representative large-table rehearsal has been run from a staging/prod-like Cassandra 3.11 source

## Local Search Closure

Local archaeology is closed for this workstation. The following checks were completed and are enough to stop further local digging:

- the older archived full-copy run exists and is real, but it is tiny:
  - total source rows: `1573`
  - largest tables only: `315 / 241 / 180 / 137`
- the old `gp3_cassandra-data` volume is `2.958GB` overall, but mostly commitlog
- relevant `rcasinoks` + `rcasinoscks` payload in that volume is only about `1,609,056 bytes`
- the largest relevant local payload found anywhere on this machine is only about `4,467,540 bytes`

These numbers are useful for documenting the search, not for approving a production cutover window.

## Decision

- keep the current migration runbook and smoke path unchanged for now
- do not switch the documented production migration mechanism based on local smoke data
- require external representative data or environment access before deciding between `cqlsh COPY` and `DSBulk`

## External Input Required

Preferred:

- a representative Cassandra 3.11 snapshot, SSTable tar, or equivalent export covering `rcasinoks` and `rcasinoscks`

Alternative:

- SSH or read-only access to a staging or production-like legacy Cassandra node so operators can capture:
  - `nodetool tablestats` or `cfstats`
  - `DESCRIBE KEYSPACE rcasinoks`
  - `DESCRIBE KEYSPACE rcasinoscks`
  - a timing rehearsal on the 1-2 largest tables

## Evidence

- release rehearsal bundle:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_093403/release_rehearsal_04.zip`
- production scale proof bundle:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_121415/prod_scale_proof_01.zip`
- local volume summary:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_121415/prod_scale_proof_01/volume_scan_summary.txt`
- related operator request:
  - `docs/refactored_release/PROD_MIGRATION_APPROVAL_REQUEST.md`
- rehearsal templates:
  - `docs/refactored_release/REHEARSAL_TEMPLATE_OPTION1_SNAPSHOT.md`
  - `docs/refactored_release/REHEARSAL_TEMPLATE_OPTION2_DSBULK.md`
