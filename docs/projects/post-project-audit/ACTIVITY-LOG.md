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
