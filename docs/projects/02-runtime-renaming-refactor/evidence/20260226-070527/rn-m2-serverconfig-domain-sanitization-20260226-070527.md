# Runtime Renaming Mini-Wave M2.4 (Server config domain sanitization)

## Objective
Remove remaining external `mqbase` domain references from active mqb server config template file and point local refactor profile to internal/local hosts.

## Files changed
- `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml`

## What changed
- Replaced external server host/domain values with local values:
  - `games.mqbase.com` -> `localhost`
  - `.mqbase.com` gsDomain -> `.localhost`
  - `.mqbase.com` domain -> `localhost`
- Replaced multiplayer lobby cluster host references:
  - `games-mp.mqbase.com` -> `127.0.0.1:16300`
- Replaced support sender email domain token:
  - `support@report.mqbase.comm` -> `support@localhost`
- Updated repeated entries across all server template blocks in the file.

## Validation matrix (PASS)
- `mvn test` in `gs-server/sb-utils`
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Runtime audit (PASS)
- `bank-template-audit.mjs` for banks `6275,6276` in multiplayer mode: PASS
- `mqb-server-config-domain-scan.txt`: 0 lines (no remaining `mqbase` host/domain tokens in file)

## Outcome
- Active mqb server config template no longer points to external `mqbase` hosts.
- Local/refactor profile is now consistent with internal/local endpoint strategy.
