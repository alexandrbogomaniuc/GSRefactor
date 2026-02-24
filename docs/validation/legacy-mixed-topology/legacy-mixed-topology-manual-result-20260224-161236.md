# Legacy Mixed-Topology Manual Result

- status: MANUAL_LAUNCH_HANDOFF_PASS
- scope: refactored GS + legacy MP/client launch/handoff validation (manual curl evidence)
- timestamp_utc: 2026-02-24T16:11:55Z
- preflight_report: /Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/legacy-mixed-topology-validation-20260224-160531.md

## Key Findings

- `Bank is incorrect` was caused by missing `subCasinoId=507` in the manual launch URL when testing banks `6274`/`6275` against `127.0.0.1`.
- After adding `subCasinoId=507`, launches progressed to wallet auth.
- Synthetic `login/sessionId` values fail as expected with `Incorrect credentials`.
- Using valid token `bav_game_session_001` produced successful GS login and MP template redirect for both banks:
  - bank `6274` (USD)
  - bank `6275` (VND)

## Manual Launch Evidence (PASS)

### Bank 6274 (USD)
- request: `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-161112-b6274-sc507-token/request-url.txt`
- response chain: `302` from GS to legacy MP template, then `200` template HTML
- evidence dir: `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-161112-b6274-sc507-token`
- body shows legacy MP template + lobby/game asset script references and MP websocket URL (`ws://localhost:6300/websocket/mplobby`)

### Bank 6275 (VND)
- request: `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-161112-b6275-sc507-token/request-url.txt`
- response chain: `302` from GS to legacy MP template, then `200` template HTML
- evidence dir: `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-161112-b6275-sc507-token`
- body shows legacy MP template + lobby/game asset script references and MP websocket URL (`ws://localhost:6300/websocket/mplobby`)

## GS Runtime Evidence

- GS logs confirm successful login/session creation and MP redirect for both token launches (`bav_game_session_001`) with `subCasinoId=507` and banks `6274/6275`.
- GS runtime is pointed to `c1-refactor:9042` and startup cache loads include `CassandraBankInfoPersister loadAll: count=3` and `CassandraCurrencyPersister loadAll: count=15`.

## Remaining Manual Checklist Work

- Reconnect scenario validation (explicit repeat/refresh flow with captured success criteria).
- FRB scenario validation if enabled for target bank/game.
- Consolidated operator-style manual run transcript (timestamps + logs per checklist step).
