# Runtime Renaming Mini-Wave M2.3 (MQ weapons mode alias seeding)

## Objective
Seed `ABS_WEAPONS_MODE` next to existing `MQ_WEAPONS_MODE` in active bank templates.

## Files changed
- `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`

## What changed
- Added `ABS_WEAPONS_MODE=LOOT_BOX` beside each `MQ_WEAPONS_MODE=LOOT_BOX` entry in active local/mqb bank blocks.
- This keeps current runtime behavior unchanged while enabling staged migration to non-MQ key names.

## Validation matrix (PASS)
- `mvn test` in `gs-server/sb-utils`
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Runtime audit (PASS)
- `bank-template-audit.mjs` for banks `6275,6276` in multiplayer mode.
- Third-party URL findings: `0`
- Third-party allow-list findings: `0`

## Outcome
- Template configs now provide dual-key compatibility for weapons mode (`MQ_*` + `ABS_*`).
- This reduces risk for later hard rename/removal waves.
