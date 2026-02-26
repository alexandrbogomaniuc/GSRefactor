# Hard-Cut M2 Wave 74 Report (GetBalanceForm)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W74-get-balance-form`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.GetBalanceForm` -> `com.abs.casino.actions.api.GetBalanceForm`

Runtime/config updates:
- `struts-config.xml` form-bean type updated to `com.abs.casino.actions.api.GetBalanceForm`
- removed legacy `com.dgphoenix...GetBalanceForm` import from `GetBalanceAction`

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetBalanceForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetBalanceAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-161441-hardcut-m2-wave74-get-balance-form`

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
- M2 Wave 75: continue with next low-fanout API form declaration.
