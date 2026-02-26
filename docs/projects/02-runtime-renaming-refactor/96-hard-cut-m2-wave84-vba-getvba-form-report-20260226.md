# Hard-Cut M2 Wave 84 Report (GetVBAForm)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W84-vba-getvba-form`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.vba.GetVBAForm` -> `com.abs.casino.actions.api.vba.GetVBAForm`

Runtime/config updates:
- `struts-config.xml` form-bean type updated to `com.abs.casino.actions.api.vba.GetVBAForm`
- added explicit `com.abs...GetVBAForm` import in `GetVBAAction` for cross-package compatibility

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/vba/GetVBAForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/vba/GetVBAAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-171713-hardcut-m2-wave84-vba-getvba-form`

## Key migration result
- Pre-scan legacy refs for wave scope: `2`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `3`

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
- VBA form wiring and action compatibility are validated by package + smoke checks.

## Next wave proposal
- M2 Wave 85: migrate `GetVBAAction` + Struts action type.
