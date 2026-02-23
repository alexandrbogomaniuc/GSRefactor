# Phase 6 Multiplayer Routing Policy Probe and Test Gate (2026-02-20 19:26 UTC)

## What was done
- Added explicit routing-policy probe for multiplayer-service to validate:
  1) `isMultiplayer=false` bypass,
  2) bank capability gate (`isMultiplayer` disabled by bank config) on multiplayer requests.
- Updated Phase 6 evidence pack to treat the policy probe as mandatory and sync-canary as optional.

## Why this change
- Prevents false failures and regressions from mixing two different test intents:
  - policy validation for non-MP-first banks (current default `6275`),
  - sync canary flow for future multiplayer-enabled banks.
- Enforces post-change testing discipline in the default Phase 6 evidence workflow.

## Files changed
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-runtime-evidence-pack.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`

## Test gate behavior
- Default (`bank 6275`) expected results:
  - non-MP request -> `routeToMultiplayerService=false`, reason `non_multiplayer_game`
  - MP request -> `routeToMultiplayerService=false`, reason `bank_multiplayer_disabled`
- Sync canary is only executed when `--run-sync-canary true` is explicitly provided.

## Validation commands
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-runtime-evidence-pack.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh --help
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-runtime-evidence-pack.sh --help
```

## Result
- Phase 6 default evidence path now validates the correct behavior for non-multiplayer-focused banks and reduces avoidable rework from test expectation mismatch.
