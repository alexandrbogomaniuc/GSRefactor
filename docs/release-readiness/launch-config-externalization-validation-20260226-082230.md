# Launch Config Externalization Validation

Date (UTC): 2026-02-26 08:22 UTC
Workspace: `/Users/alexb/Documents/Dev/Dev_new`

## Goal
Move refactor launch defaults out of script hardcoding into external config.

## What changed
- Launch defaults are now read from:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`
- Scripts updated:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-start.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-onboard.mjs`
- Environment variables with the same names still override config file values.

## Validation
- Shell syntax check (start script): PASS
- Node syntax check (onboard script): PASS
- Runtime smoke check with configured primary + secondary launch URLs: PASS
- Runtime smoke check with env override (`LAUNCH_BANK_ID=6274`, `LAUNCH_SUBCASINO_ID=508`): PASS

Evidence:
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-082230/refactor-onboard-smoke.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-082230/refactor-onboard-smoke-env-override.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-082230/launch-config-keys.txt`

## Result
Launch behavior is now configured outside code by default, matching the non-hardcoded requirement.
