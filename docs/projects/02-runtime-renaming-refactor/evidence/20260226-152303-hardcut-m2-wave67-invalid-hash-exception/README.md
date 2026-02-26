# Evidence - Hard-Cut M2 Wave 67 (InvalidHashException)

Date (UTC): 2026-02-26
Wave: `W67-invalid-hash-exception`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.InvalidHashException` -> `com.abs.casino.actions.api.InvalidHashException`

Scope adjustments:
- No dependent import rewrites were required; class had no external usages in current tree.

## Scan result
- pre legacy refs: 1
- post legacy refs: 0
- post abs refs: 1

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

## Notes
- `web-gs` package step runs with explicit `-Dcluster.properties=common.properties`.
- MP reactor module selector remains `core,persistance` (repo naming).
