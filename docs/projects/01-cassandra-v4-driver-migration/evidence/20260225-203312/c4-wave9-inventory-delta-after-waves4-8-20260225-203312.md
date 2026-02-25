# CASS-V4 Wave 9: Post-wave inventory delta checkpoint

Date (UTC): 2026-02-25 21:06
Project: `CASS-V4`

## Scope
Refresh the Cassandra driver inventory after waves 4-8 and capture measurable reduction in driver3 usage to validate migration direction.

## Inventory artifact
- `phase7-cassandra-driver-inventory-20260225-210819.txt`

## Delta vs Wave 1 baseline inventory
Compared files:
- baseline: `phase7-cassandra-driver-inventory-20260225-203429.txt`
- refreshed: `phase7-cassandra-driver-inventory-20260225-210819.txt`

Main deltas:
- GS `driver3_import_lines`: `488 -> 478` (`-10`)
- GS `querybuilder` import-type count: `176 -> 172` (`-4`)
- GS `Session` import-type count: `21 -> 18` (`-3`)
- GS `Host` import-type count: `5 -> 0` (`-5` in top categories)
- MP remains unchanged at this stage:
  - `driver3_import_lines`: `151 -> 151`

## What this means
- The recent compatibility/decoupling waves are reducing driver3 coupling in GS incrementally and safely.
- The largest remaining migration surface is still in `common-persisters` and `AbstractCassandraPersister` consumers.

## Next target
- Continue hotspot-by-hotspot migration in `common-persisters` (high-frequency `querybuilder`, `Row`, `ResultSet` usage), then begin staged MP persister migration.
