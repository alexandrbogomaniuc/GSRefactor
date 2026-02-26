# Hard-Cut M2 Wave 73 Report (GetBalanceAction)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W73-get-balance-action`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.GetBalanceAction` -> `com.abs.casino.actions.api.GetBalanceAction`

Runtime mapping update:
- `struts-config.xml` action type updated to `com.abs.casino.actions.api.GetBalanceAction`

Compatibility updates:
- Added explicit import for `com.dgphoenix.casino.actions.api.GetBalanceForm`

Wave touched 2 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetBalanceAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-160747-hardcut-m2-wave73-get-balance-action`

## Key migration result
- Pre-scan legacy refs for wave scope: `1`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `1`

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
- Struts mapping was updated in-wave, and validation/smoke passed with no extra compatibility fixes.

## Next wave proposal
- M2 Wave 74: continue with next low-fanout API action/form declaration.
