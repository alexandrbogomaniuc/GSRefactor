# Hard-Cut M2 Wave 91 Report (bonus response batch)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W91-bonus-response-batch`
Status: `COMPLETE`

## Scope
Migrated namespace for package family:
- `com.dgphoenix.casino.actions.api.bonus.response` -> `com.abs.casino.actions.api.bonus.response`

Classes migrated:
- `BaseBonus`
- `Bonus`
- `FRBonus`
- `JSONResponse`

Dependent wiring update:
- `AbstractBonusAction` import updated to `com.abs...JSONResponse`

Wave touched 5 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/BaseBonus.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/Bonus.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/FRBonus.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/JSONResponse.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/AbstractBonusAction.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-180634-hardcut-m2-wave91-bonus-response-batch`

## Key migration result
- Pre-scan legacy refs for wave scope: `6`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `6`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (with explicit `-Dcluster.properties=common.properties`)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Batch was confined to one package family and one dependent import bridge.

## Next wave proposal
- M2 Wave 92: migrate next low-fanout form/action pair with bounded Struts wiring change.
