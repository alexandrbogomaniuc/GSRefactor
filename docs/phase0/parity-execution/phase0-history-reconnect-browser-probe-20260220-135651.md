# Phase 0 History/Reconnect Probe (Browser, facade path)

- Timestamp (UTC): 2026-02-20 13:52-13:56
- Probe origin: `http://localhost:18080`
- Method: Chrome DevTools browser navigation + in-page fetch checks

| Test ID | Flow | Status | HTTP | Evidence |
|---|---|---|---|---|
| P0-HI-01 | HistoryByRound (`ROUNDID=1`) | PASS_PROBE | 200 | returns `Game history not found` page, route alive |
| P0-HI-02 | HistoryByToken missing (`token=dummy`) | PASS_PROBE | 200 | returns deterministic error page, no route break |
| P0-HI-03 | CWStartHistory (`bankId=271&token=test_user_271`) | PASS_PROBE | 200 | returns deterministic error page, no route break |
| P0-RC-01 | RestartGame valid bank/sessionId route | FAIL_ROUTE | 404 | redirected target `http://localhost/cwstartgame.do?...` not found |
| P0-RC-02 | RestartGame invalid bank | FAIL_HTTP | 500 | Jetty stack trace shows `NullPointerException` in `RestartGameAction.java:38` |

## Findings
1. History routes are reachable and deterministic on facade path.
2. Reconnect/restart route has two parity gaps:
   - valid-bank restart currently resolves to a missing legacy start URL path (`/cwstartgame.do`) on this runtime branch,
   - invalid-bank restart throws NPE instead of returning controlled error semantics.

## Remediation prepared (source)
- Added backward-compatible `cwstartgame` mapping with sessionId-based token extraction for restart flow:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameBySessionForm.java`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Added invalid-bank guard in restart action to avoid NPE and route to controlled launch error path:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/frb/RestartGameAction.java`

## Note
- Remediation is source-level and requires rebuild/redeploy of refactor GS runtime before runtime parity re-check.
