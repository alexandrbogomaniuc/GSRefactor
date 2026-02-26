# Hard-Cut M2 Wave 92 Report (history vba actions)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W92-history-vba-actions`
Status: `COMPLETE`

## Scope
Migrated namespaces:
- `com.dgphoenix.casino.actions.api.history.vba.HistoryByRoundAction` -> `com.abs.casino.actions.api.history.vba.HistoryByRoundAction`
- `com.dgphoenix.casino.actions.api.history.vba.HistoryByTokenAction` -> `com.abs.casino.actions.api.history.vba.HistoryByTokenAction`

Runtime/config updates:
- Struts action type rewiring:
  - `/vabs/historyByRound`
  - `/vabs/historyByToken`

Wave touched 3 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/history/vba/HistoryByRoundAction.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/history/vba/HistoryByTokenAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-181059-hardcut-m2-wave92-history-vba-actions`

## Key migration result
- Pre-scan legacy refs for wave scope: `4`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `4`

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
- Action + Struts mapping were migrated together; smoke checks remained green.

## Next wave proposal
- M2 Wave 93: migrate next low-fanout form/action pair in API history/bonus scopes.
