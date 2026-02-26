# Hard-Cut M2 Wave 85 Report (GetVBAAction)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W85-vba-getvba-action`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.vba.GetVBAAction` -> `com.abs.casino.actions.api.vba.GetVBAAction`

Runtime/config updates:
- `struts-config.xml` action type updated to `com.abs.casino.actions.api.vba.GetVBAAction`

Wave touched 2 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/vba/GetVBAAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-172248-hardcut-m2-wave85-vba-getvba-action`

## Key migration result
- Pre-scan legacy refs for wave scope: `2`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `2`

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
- Action wiring is validated by package + smoke checks.

## Next wave proposal
- M2 Wave 86: migrate next low-fanout API class with bounded references.
