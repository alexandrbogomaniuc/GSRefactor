# Non-Prod Validation Refresh

Last updated (UTC): 2026-02-26 07:42

## Purpose

This refresh confirms the refactor environment is still runnable after the latest closure/documentation updates.

## Commands Run

1. `node ./gs-server/deploy/scripts/refactor-onboard.mjs preflight`
2. `node ./gs-server/deploy/scripts/refactor-onboard.mjs smoke`
3. `curl -i 'http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en'`

## Result (Simple English)

- Preflight: `PASS`
- Smoke: `PASS`
- Launch alias `/startgame`: `HTTP 200`

This confirms the current non-production environment is healthy and launchable for the reference bank/game flow.

## Evidence Files

- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074218/refactor-onboard-preflight.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074218/refactor-onboard-smoke.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074218/startgame-head-20260226-074218.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074218/docker-ps.txt`

