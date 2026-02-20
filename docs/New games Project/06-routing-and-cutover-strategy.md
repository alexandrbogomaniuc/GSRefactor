# Routing And Cutover Strategy

Last updated: 2026-02-12

## Goal
Add New Games as an external module without breaking legacy flows.

## Routing Rule (Phase 1)
- Route to New Games when effective `gameId` is `00010` (default rule).
- This rule is bank-configurable through bank property:
  - `NEW_GAMES_ROUTE_GAME_ID`
- Else route to legacy flow unchanged.

## Integration Point In GS
- Apply routing decision in game launch action before final launch URL is returned.
- Keep same GS auth/session checks for both paths.
- For New Games route specifically, skip legacy game-template/wallet-op validation that requires a legacy game catalog entry.
  This allows virtual route ids (for example `00010`) to launch even when not present in legacy `gameinfocf`.

## Launch Result By Route
1. Legacy route:
- existing GS + MP/static flow.

2. New route (`00010`):
- GS builds launch payload for NGS client.
- client talks to NGS API for gameplay commands.
- NGS calls GS internal APIs for session/wallet/history.
- GS login/session creation still happens in GS, but legacy game-specific preflight checks are bypassed for this route.

Live evidence (2026-02-12):
- `GET /cwstartgamev2.do?bankId=6274&gameId=00010&mode=real&token=bav_game_session_001&lang=en`
- response: `302 Found`
- location target contains expected NGS launch params:
  - `http://localhost:5174/?gameId=00010&ngsContract=v1&gameIdNumeric=10&MODE=real&GAMESERVERID=1&ngsApiUrl=http://localhost:6400&LANG=en&BANKID=6274&SID=...&gsInternalBaseUrl=http://localhost:81`
- full API lifecycle with real GS-internal bridge was validated:
  - `POST http://localhost:81/gs-internal/newgames/v1/session/validate` -> `200`
  - `POST http://localhost:6400/v1/opengame` -> `200`
  - `POST http://localhost:6400/v1/placebet` -> `200`
  - `POST http://localhost:6400/v1/collect` -> `200`
  - `POST http://localhost:6400/v1/readhistory` -> `200`
- GS logs for the same flow show:
  - casino wallet reserve/settle calls via `.../bav/betResult`
  - `NGS_HISTORY_WRITE` for `BET_PLACED` and `ROUND_COLLECTED`

## Bank-Level Config Keys
- `NEW_GAMES_ROUTE_GAME_ID`: route matcher (default fallback `00010`).
- `NEW_GAMES_CLIENT_URL`: launch URL for new-games-client (example `http://localhost:5174/`).
- `NEW_GAMES_API_URL`: client API base for NGS (example `http://localhost:6400`).
- `NEW_GAMES_GS_INTERNAL_BASE_URL`: GS internal API base used by NGS (example `http://localhost:81`).

## Safety Controls
- Feature flag: `NEW_GAMES_ROUTING_ENABLED`.
- Kill switch: immediate fallback all traffic to legacy route.
- Bank allow-list: enable only selected banks/subcasinos at beta.

## Rollout Plan
1. Local/dev only.
2. Internal test bank only.
3. One production bank with low exposure.
4. Wider rollout after stability targets are met.

## Metrics Required Before Expansion
- route split volume (`legacy` vs `new`).
- NGS `placebet` success rate.
- GS wallet reserve/settle error rate.
- duplicate operation rate (must stay zero).
