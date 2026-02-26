# Evidence - Hard-Cut M2 Wave 64 (IStartGameHelper)

Date (UTC): 2026-02-26
Wave: `W64-istartgamehelper`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.IStartGameHelper` -> `com.abs.casino.common.games.IStartGameHelper`

Scope adjustments:
- Updated dependent imports in `IHelperCreator`, `StartGameHelpers`, `AbstractStartGameHelper`, `CommonWalletManager`, `DBLink`, `StartGameServletFilter`, `GameServer`, and `web-gs/support/listGameIds.jsp`.

## Scan result
- pre legacy refs: 6
- post legacy refs: 0
- post abs refs: 8

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

## Notes
- `web-gs` package step runs with explicit `-Dcluster.properties=common.properties`.
- MP reactor module selector remains `core,persistance` (repo naming).
