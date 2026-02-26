# Hard-Cut M2 Wave 93 Report (FRB cancel form)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W93-frbonus-cancel-form`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.frbonus.CancelFRBForm` -> `com.abs.casino.actions.api.frbonus.CancelFRBForm`

Runtime/config updates:
- `CancelFRBAction` import updated to `com.abs...CancelFRBForm`.
- Struts form-bean `FRBCancelForm` type updated to `com.abs...CancelFRBForm`.

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-181509-hardcut-m2-wave93-frbonus-cancel-form`

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
- Form class + action import + Struts form-bean mapping were migrated together.

## Next wave proposal
- M2 Wave 94: migrate next FRB form with bounded action import + Struts form-bean update.
