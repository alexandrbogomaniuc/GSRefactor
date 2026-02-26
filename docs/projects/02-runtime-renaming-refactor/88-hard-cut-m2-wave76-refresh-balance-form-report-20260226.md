# Hard-Cut M2 Wave 76 Report (RefreshBalanceForm)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W76-refresh-balance-form`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.RefreshBalanceForm` -> `com.abs.casino.actions.api.RefreshBalanceForm`

Runtime/config updates:
- `struts-config.xml` form-bean type updated to `com.abs.casino.actions.api.RefreshBalanceForm`
- removed legacy `com.dgphoenix...RefreshBalanceForm` import from `RefreshBalanceAction`

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/RefreshBalanceForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/RefreshBalanceAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-162735-hardcut-m2-wave76-refresh-balance-form`

## Key migration result
- Pre-scan legacy refs for wave scope: `2`
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
- Form bean wiring and dependent action compilation are validated by package + smoke checks.

## Next wave proposal
- M2 Wave 77: continue with next low-fanout API form/action declaration.
