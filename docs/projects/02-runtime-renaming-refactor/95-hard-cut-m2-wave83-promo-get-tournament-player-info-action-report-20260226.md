# Hard-Cut M2 Wave 83 Report (GetTournamentPlayerInfoAction)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W83-promo-get-tournament-player-info-action`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.promo.GetTournamentPlayerInfoAction` -> `com.abs.casino.actions.api.promo.GetTournamentPlayerInfoAction`

Runtime/config updates:
- `struts-config.xml` action type updated to `com.abs.casino.actions.api.promo.GetTournamentPlayerInfoAction`

Wave touched 2 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-171229-hardcut-m2-wave83-promo-get-tournament-player-info-action`

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
- M2 Wave 84: migrate next low-fanout promo/api class with bounded references.
