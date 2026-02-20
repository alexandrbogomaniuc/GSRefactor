# Phase 0 Startgame Alias Verification

- Timestamp (UTC): 2026-02-20 10:50:11
- Endpoint alias target: /startgame -> /cwstartgamev2.do (internal proxy)

## Results
- proxy /startgame: HTTP 200
- proxy /cwstartgamev2.do: HTTP 200
- direct gs /startgame (18081): HTTP 404 (alias intentionally handled at proxy layer)
- redirect visibility: no `Location` header in `/startgame` response

## Artifacts
- /startgame headers: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/startgame-alias-20260220-105011.headers.txt`
- /startgame body: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/startgame-alias-20260220-105011.body.txt`
- /cwstartgamev2.do headers: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/cwstartgamev2-20260220-105011.headers.txt`
- /cwstartgamev2.do body: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/cwstartgamev2-20260220-105011.body.txt`
- direct gs /startgame headers: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/startgame-direct-gs-20260220-105011.headers.txt`
- direct gs /startgame body: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/startgame-direct-gs-20260220-105011.body.txt`
