# Hard-Cut M2 Wave 75 Report (GetLeaderboardUrlsForm)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W75-get-leaderboard-urls-form`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.GetLeaderboardUrlsForm` -> `com.abs.casino.actions.api.GetLeaderboardUrlsForm`

Runtime/config updates:
- `struts-config.xml` form-bean type updated to `com.abs.casino.actions.api.GetLeaderboardUrlsForm`
- removed legacy `com.dgphoenix...GetLeaderboardUrlsForm` import from `GetLeaderboardUrlsAction`

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetLeaderboardUrlsForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetLeaderboardUrlsAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-162212-hardcut-m2-wave75-get-leaderboard-urls-form`

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
- M2 Wave 76: continue with next low-fanout API form/action declaration.
