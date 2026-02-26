# Evidence - Hard-Cut M2 Wave 61 (SwfLocationInfo)

Date (UTC): 2026-02-26
Wave: `W61-swf-location-info`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.SwfLocationInfo` -> `com.abs.casino.common.games.SwfLocationInfo`

Scope adjustments:
- Updated dependent imports in `IStartGameHelper`, `NewTranslationGameHelper`, and `StartGameServletFilter`.

## Scan result
- pre legacy refs: 1
- post legacy refs: 0
- post abs refs: 3

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

## Notes
- `web-gs` package step runs with explicit `-Dcluster.properties=common.properties`.
- MP reactor module selector remains `core,persistance` (repo naming).
