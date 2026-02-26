# Evidence - Hard-Cut M2 Wave 60 (ICassandraHostCdnPersister)

Date (UTC): 2026-02-26
Wave: `W60-icassandra-host-cdn-persister`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.ICassandraHostCdnPersister` -> `com.abs.casino.common.games.ICassandraHostCdnPersister`

Scope adjustments:
- Updated dependent imports in `NewTranslationGameHelper`, `AbstractStartGameHelper`, and `CassandraHostCdnPersister`.

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
