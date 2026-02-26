# Evidence - Hard-Cut M2 Wave 63 (StartGameHelpers)

Date (UTC): 2026-02-26
Wave: `W63-start-game-helpers`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.StartGameHelpers` -> `com.abs.casino.common.games.StartGameHelpers`

Scope adjustments:
- Updated dependent imports in `CommonWalletManager`, `DBLink`, `StartGameServletFilter`, and `web-gs/support/listGameIds.jsp`.
- Added explicit `IStartGameHelper` import in migrated `StartGameHelpers` class.

## Scan result
- pre legacy refs: 4
- post legacy refs: 0
- post abs refs: 4

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

## Notes
- `web-gs` package step runs with explicit `-Dcluster.properties=common.properties`.
- MP reactor module selector remains `core,persistance` (repo naming).
