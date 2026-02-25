# CASS-V4 Wave 2: Migration Backlog Automation

Date (UTC): 2026-02-25 20:38
Project: `CASS-V4`

## Scope
Create a repeatable backlog generator so driver4 migration is executed by priority (highest-risk, highest-impact modules first).

## Implementation
- Added script:
  - `gs-server/deploy/scripts/phase7-cassandra-driver-migration-backlog.sh`
- Script capabilities:
  - dynamic repo-root resolution,
  - scans GS + MP Java driver3 imports,
  - outputs markdown backlog with:
    - file counts by module hotspot,
    - top files by driver3 import density,
    - driver3 API type frequency,
    - recommended migration order,
    - starter API mapping (driver3 -> driver4).

## Validation
- `bash -n` syntax check: PASS
- script execution: PASS
- generated artifact:
  - `phase7-cassandra-driver-migration-backlog-20260225-203850.md`

## Key output highlights
- driver3 import files: 163 total (`GS=128`, `MP=35`)
- primary hotspots:
  - `gs-server/cassandra-cache/common-persisters` (69 files)
  - `mp-server/persistance` (35 files)
  - `gs-server/cassandra-cache/cache` (27 files)
  - `gs-server/promo/persisters` (17 files)

## Decision
Wave 2 backlog automation is complete. Next implementation wave should migrate `gs-server/cassandra-cache/cache` first, then `common-persisters`, then `mp-server/persistance`.
