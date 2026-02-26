# Evidence - Hard-Cut M2 Wave 27 (IServerInfoInternalProvider)

Date (UTC): 2026-02-26
Wave: `W27-server-info-internal-provider`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.server.IServerInfoInternalProvider` -> `com.abs.casino.common.cache.data.server.IServerInfoInternalProvider`

Scope adjustments:
- Updated dependent imports in `LoadBalancerCache`, `ServerConfigsCache`, and `CassandraServerInfoPersister`.
- Added explicit `ServerInfo` import inside the migrated interface to preserve compatibility while `ServerInfo` remains in `com.dgphoenix`.

## Scan result
- legacy refs after wave: 0
- abs refs after wave: 3

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`
