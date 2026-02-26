# Activity Log

Project: Post-Project Audit

## 2026-02-25 20:16 UTC
- Created per-project activity log as requested.
- Confirmed audit package is now grouped in this folder, including `audit-evidence/` and all milestone/finalization docs.
- Status: audit docs organized; optional next wave is remaining top-level numbered docs migration.

## 2026-02-26 08:01 UTC
- Resolved runtime support portal availability gap for post-project audit dashboard.
- Root cause: runtime support mount was stale and missed modernization files; startup did not re-sync support assets after prior runs.
- Fix: patched `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-start.sh` to copy support pages + audit JSON data into runtime support path during each `up`.
- Evidence captured in:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080102/`
- Outcome: `http://127.0.0.1:18081/support/modernizationProgress.html` returns `HTTP 200` and displays current audit portal content.

## 2026-02-26 08:06 UTC
- Fixed a real runtime cutover blocker in local refactor gameplay flow:
  - game websocket URL used internal MP port (`6300`) instead of exposed refactor port (`16300`).
- Patched MP room URL generation in:
  - `/Users/alexb/Documents/Dev/Dev_new/mp-server/web/src/main/java/com/betsoft/casino/mp/web/handlers/lobby/AbstractStartGameUrlHandler.java`
- Build and runtime verification completed:
  - MP web module build PASS,
  - MP container restarted,
  - browser launch now resolves iframe game websocket to `ws://127.0.0.1:16300/websocket/mpgame`,
  - MP logs show successful `GetStartGameUrl`, `OpenRoom`, and active gameplay pings.
- Evidence package:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/mp-websocket-external-port-fix-validation-20260226-080619.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080619/`

## 2026-02-26 08:17 UTC
- Closed launch-url ambiguity for internal Betonline bank route by documenting and validating the real runtime mapping.
- Runtime finding:
  - direct `bankId=6276` launch on subcasino `508` returns `Bank is incorrect`;
  - working launch path is `bankId=6274&subCasinoId=508` for the same internal bank context.
- Hardened startup and onboarding scripts to make launch values configurable (no hidden fixed launch tuple) and added optional secondary launch smoke checks:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-start.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-onboard.mjs`
- Updated stakeholder onboarding docs with explicit mapping guidance and env override examples:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/START-HERE-REFRACTOR.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/README-ONBOARDING.md`
- Evidence package:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/launch-bank-id-mapping-validation-20260226-081724.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-081724/`

## 2026-02-26 08:22 UTC
- Implemented outside-config launch defaults for onboarding/start scripts to reduce remaining hardcoded behavior.
- Added launch config keys to centralized file:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`
- Updated scripts to consume launch defaults from centralized config (env vars still override):
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-start.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-onboard.mjs`
- Updated onboarding docs to show where launch defaults are managed outside code.
- Validation evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/launch-config-externalization-validation-20260226-082230.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-082230/refactor-onboard-smoke.log`
