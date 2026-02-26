# Hard-Cut M2 Wave 78 Report (GetLeaderboardResultsForm)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W78-get-leaderboard-results-form`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.mq.GetLeaderboardResultsForm` -> `com.abs.casino.actions.api.mq.GetLeaderboardResultsForm`

Runtime/config updates:
- `struts-config.xml` form-bean type updated to `com.abs.casino.actions.api.mq.GetLeaderboardResultsForm`
- added `com.abs...GetLeaderboardResultsForm` import in `GetLeaderboardResultsAction` for cross-package compatibility

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardResultsForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardResultsAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-164235-hardcut-m2-wave78-get-leaderboard-results-form`

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
- Form bean wiring and action compatibility are validated by package + smoke checks.

## Next wave proposal
- M2 Wave 79: migrate `GetLeaderboardResultsAction` and its Struts action mapping type.
