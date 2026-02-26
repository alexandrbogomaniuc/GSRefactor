# Runtime Renaming Mini-Wave M2.2 (Alias key seeding in bank templates)

## Objective
Seed ABS_* compatibility keys in active bank templates so runtime can read either legacy keys or new alias keys during staged rename.

## Files changed
- `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`

## What changed
Added alias entries next to existing runtime-critical keys:
- `ABS_WPM_CLASS`
- `ABS_CLOSE_GAME_PROCESSOR`
- `ABS_START_GAME_PROCESSOR` (mqb profile where start processor key exists)

Values mirror the current effective legacy values, so runtime behavior is unchanged while making configs rename-ready.

## Validation matrix (PASS)
- `mvn test` in `gs-server/sb-utils`
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Runtime audit (PASS)
- `bank-template-audit.mjs` for banks `6275,6276` in multiplayer mode: PASS
- Third-party URL findings: 0
- Third-party allow-list findings: 0

## Outcome
- Bank templates now carry both legacy and ABS alias keys for key class-string properties.
- This de-risks the later hard rename step by ensuring config-level compatibility is already in place.
