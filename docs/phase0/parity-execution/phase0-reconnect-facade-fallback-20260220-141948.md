# Phase 0 Reconnect Facade Fallback Validation

Timestamp (UTC): 2026-02-20 14:19:48 UTC
Scope: temporary compatibility fallback on refactor static facade to remove browser-visible redirects and keep legacy reconnect behavior stable while GS source rebuild is pending.

## Changes applied
- File: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/static/games`
- Added `location = /restartgame.do` interception with internal redirect follow.
- Added redirect normalization in `@follow_gs_redirect`:
  - `/cwstartgame.do?...` -> `/cwstartgamev2.do?...`
  - `sessionId=...` mapped to `token=...`.
- Added explicit compatibility endpoint `location = /cwstartgame.do` -> `/cwstartgamev2.do` with `sessionId -> token` mapping.
- Added temporary `error_page 500 = @restartgame_fallback` for `/restartgame.do` to avoid raw 500 pages at facade boundary.

## Runtime rollout evidence
- Build/recreate static container (refactor only):
  - `docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml build static`
  - `docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml up -d --no-deps --force-recreate static`
- Active nginx config check:
  - `docker exec refactor-static-1 sh -lc 'sed -n "24,80p" /etc/nginx/sites-enabled/games'`
  - confirms `location = /restartgame.do`, `location = /cwstartgame.do`, and fallback blocks are loaded.

## Browser parity probe (facade origin)
Probe origin: `http://localhost:18080`

| Probe ID | Request | Result |
|---|---|---|
| RC_VALID | `/restartgame.do?bankId=271&gameId=838&sessionId=test_user_271&mode=real&lang=en` | HTTP `200`, title `Max Quest: Dragonstone` |
| RC_INVALID_BANK | `/restartgame.do?bankId=999999&gameId=838&sessionId=invalid_session&mode=real&lang=en` | HTTP `200`, controlled `Error` page (no raw Jetty 500) |
| CW_LEGACY | `/cwstartgame.do?bankId=271&gameId=838&sessionId=test_user_271&mode=real&lang=en` | HTTP `200`, title `Max Quest: Dragonstone` |
| CW_V2 | `/cwstartgamev2.do?bankId=271&gameId=838&token=test_user_271&mode=real&lang=en` | HTTP `200`, title `Max Quest: Dragonstone` |

Network verification (same page context):
- `reqid=23` restartgame valid -> `200`
- `reqid=26` restartgame invalid bank -> `200`
- `reqid=27` cwstartgame legacy -> `200`
- No `302` redirect entries for these final probes.

## Compatibility note
- This is a facade-level compatibility bridge only; GS source-level fix in `RestartGameAction` remains required for definitive backend parity.
- Behavior remains backward-compatible: legacy paths stay callable, with redirects hidden from browser-visible flow.
