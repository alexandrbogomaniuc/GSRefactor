# Evidence - Hard-Cut M2 Wave 65 (NewTranslationGameHelper)

Date (UTC): 2026-02-26
Wave: `W65-new-translation-game-helper`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.NewTranslationGameHelper` -> `com.abs.casino.common.games.NewTranslationGameHelper`

Scope adjustments:
- Updated class inheritance import to `com.dgphoenix.casino.common.games.AbstractStartGameHelper` after package move.
- Updated `GameServer` import to `com.abs.casino.common.games.NewTranslationGameHelper`.

## Scan result
- pre legacy refs: 1
- post legacy refs: 0
- post abs refs: 2

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

## Notes
- `web-gs` package step runs with explicit `-Dcluster.properties=common.properties`.
- MP reactor module selector remains `core,persistance` (repo naming).
