# Evidence - Hard-Cut M2 Wave 62 (CdnCheckResult)

Date (UTC): 2026-02-26
Wave: `W62-cdn-check-result`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.CdnCheckResult` -> `com.abs.casino.common.games.CdnCheckResult`

Scope adjustments:
- Updated dependent imports in `ICassandraHostCdnPersister`, `NewTranslationGameHelper`, `CassandraHostCdnPersister`, and `web-gs/cdn/info.jsp`.

## Scan result
- pre legacy refs: 3
- post legacy refs: 0
- post abs refs: 3

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

## Notes
- `web-gs` package step runs with explicit `-Dcluster.properties=common.properties`.
- MP reactor module selector remains `core,persistance` (repo naming).
