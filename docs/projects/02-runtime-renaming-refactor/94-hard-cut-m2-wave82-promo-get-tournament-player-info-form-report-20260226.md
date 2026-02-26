# Hard-Cut M2 Wave 82 Report (GetTournamentPlayerInfoForm)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W82-promo-get-tournament-player-info-form`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.promo.GetTournamentPlayerInfoForm` -> `com.abs.casino.actions.api.promo.GetTournamentPlayerInfoForm`

Runtime/config updates:
- `struts-config.xml` form-bean type updated to `com.abs.casino.actions.api.promo.GetTournamentPlayerInfoForm`
- added explicit `com.abs...GetTournamentPlayerInfoForm` import in `GetTournamentPlayerInfoAction` for cross-package compatibility

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-170702-hardcut-m2-wave82-promo-get-tournament-player-info-form`

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
- Form bean wiring and promo action compatibility are validated by package + smoke checks.

## Next wave proposal
- M2 Wave 83: migrate next low-fanout promo/api class with bounded references.
