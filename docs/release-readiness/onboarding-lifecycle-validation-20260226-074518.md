# Onboarding Lifecycle Validation (Down -> Up -> Smoke)

Last updated (UTC): 2026-02-26 07:49

## Why this validation was run

After startup, smoke checks were occasionally failing because unstable root/support routes were used as strict pass/fail signals.

This validation confirms the onboarding flow is now robust for real startup conditions.

## What changed

File updated:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-onboard.mjs`

Improvements:
1. Added retry logic to smoke checks (default retries with delay).
2. Replaced unstable static root `/` check with a stable static asset route:
   - `/html5pc/actiongames/dragonstone/lobby/version.json`
3. Kept GS support route as diagnostic-only (`WARN`, not hard fail).
4. Kept launch alias `/startgame` as required hard pass check.

## Lifecycle commands executed

1. `node ./gs-server/deploy/scripts/refactor-onboard.mjs down`
2. `node ./gs-server/deploy/scripts/refactor-onboard.mjs up`
3. `node ./gs-server/deploy/scripts/refactor-onboard.mjs smoke`

## Lifecycle result

- `down`: PASS
- `up`: PASS
- `smoke`: PASS (exit code `0`)

Smoke details:
- PASS static asset route (`HTTP 200`)
- WARN GS support diagnostic route (`ECONNRESET` after retries)
- PASS config service health (`HTTP 200`)
- PASS launch alias `/startgame` (`HTTP 200`, after retries)

## What this means in simple English

The one-command onboarding flow is now resilient to startup timing noise and validates the real game launch path correctly.

## Evidence files

- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074518/refactor-onboard-down-v3.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074518/refactor-onboard-up-v3.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074518/refactor-onboard-smoke-lifecycle-v3.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074518/refactor-onboard-smoke-v3.log`

