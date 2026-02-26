# Evidence - Hard-Cut M2 Wave 66 (AbstractStartGameHelper)

Date (UTC): 2026-02-26
Wave: `W66-abstract-start-game-helper`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.AbstractStartGameHelper` -> `com.abs.casino.common.games.AbstractStartGameHelper`

Scope adjustments:
- Removed legacy import from `NewTranslationGameHelper` after package alignment.

## Scan result
- pre legacy refs: 2
- post legacy refs: 0
- post abs refs: 1

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

## Notes
- `web-gs` package step runs with explicit `-Dcluster.properties=common.properties`.
- MP reactor module selector remains `core,persistance` (repo naming).
