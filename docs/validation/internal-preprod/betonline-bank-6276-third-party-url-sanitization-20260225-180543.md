# Betonline Bank 6276 Third-Party URL Sanitization (Internal Pre-Prod)

Date (UTC): 2026-02-25 18:05:43 UTC

## Purpose
Disable all third-party online URLs for the new local/internal test bank (`6276`) as requested.

Plain English:
- This bank was cloned from an older template.
- The clone copied old internet URLs (`wallet.mqbase.com`) that should not be used in local/internal testing.
- We disabled them and confirmed the game still launches.

## Bank Scope
- Subcasino: `508` (`Betonline`)
- Internal bank: `6276` (`betonline_test`)
- Internal test launch compatibility uses external wallet bank ID `6274` (known limitation already documented)

## What We Checked
Support page used:
- `http://127.0.0.1:18081/support/bankSelectAction.do?bankId=6276`

Comparison sources used for baseline and follow-up cleanup:
- Bank `6274` support page
- Bank `6275` support page

## What Was Found Before Sanitization (cloned legacy values)
The cloned template inherited these third-party URLs (same pattern originally present on banks `6274` and `6275` before this cleanup pass):

- `UPDATE_PLAYER_STATUS_IN_PRIVATE_ROOM_URL` -> `https://wallet.mqbase.com/updateStatusForPlayer`
- `UPDATE_PLAYERS_ROOMS_NUMBER_URL` -> `https://wallet.mqbase.com/updateRoomsPlayers`
- `GET_FRIENDS_URL` -> `https://wallet.mqbase.com/getFriends`
- `INVATE_PLAYERS_TO_PRIVATE_ROOM_URL` -> `https://wallet.mqbase.com/invitePlayersToPrivateRoom`
- `GET_PLAYERS_ONLINE_STATUS_URL` -> `https://wallet.mqbase.com/getOnlineStatus`
- `NOTIFICATION_CLOSE_GAME_PROCESSOR_URL` -> `https://wallet.mqbase.com/setGameSessionSummary`

Also inherited:
- `ALLOWED_ORIGIN` contained `https://mqbase.com`
- `ALLOWED_DOMAINS` contained `mqbase.com`

## Sanitization Applied to Bank 6276
Disabled/cleared all third-party online integrations:

### Cleared URL fields
- `UPDATE_PLAYER_STATUS_IN_PRIVATE_ROOM_URL`
- `UPDATE_PLAYERS_ROOMS_NUMBER_URL`
- `GET_FRIENDS_URL`
- `INVATE_PLAYERS_TO_PRIVATE_ROOM_URL`
- `GET_PLAYERS_ONLINE_STATUS_URL`
- `NOTIFICATION_CLOSE_GAME_PROCESSOR_URL`

### Disabled related feature flags
- `ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM = false`
- `USE_WINNER_FEED = false`
- `NEEDS_JACKPOT3_FEED = false`

### Cleaned browser-origin/domain allow lists
- Removed `https://mqbase.com` from `ALLOWED_ORIGIN`
- Removed `mqbase.com` from `ALLOWED_DOMAINS`

## What Was Intentionally Kept (local/internal services)
These are not third-party internet URLs and were kept:

- Wallet/BAV local endpoints (`host.docker.internal:8000`)
  - `COMMON_WALLET_AUTH_URL`
  - `COMMON_WALLET_BALANCE_URL`
  - `COMMON_WALLET_WAGER_URL`
  - `COMMON_WALLET_REFUND_URL`
  - `FR_BONUS_WIN_URL`
- Local MP endpoint
  - `MP_LOBBY_WS_URL = localhost:6300`
- Local support error page
  - `FATAL_ERROR_PAGE_URL = http://localhost:8081/game_error`

## Verification Result (Plain English)
- After sanitization, bank `6276` had **zero third-party URLs** in the checked support fields.
- The launch URL still worked (`HTTP 200`), so disabling those external URLs did not break the tested local game launch.
- The same third-party URL cleanup was then applied to banks `6274` and `6275` (existing internal test banks), and both launch smoke checks also stayed `HTTP 200`.

## Working Launch Re-Check (after sanitization)
- `http://127.0.0.1:18080/startgame?bankId=6274&subCasinoId=508&gameId=838&mode=real&token=bav_game_session_001&lang=en`

## Why This Matters
- Cloned banks should not silently call internet services during local/internal testing.
- This reduces unexpected failures, data leakage risk, and confusing false alarms.

## Next Recommended Step
- Use the new singleplayer/multiplayer template policy to avoid manual cleanup on every cloned bank.
- Optional follow-up: automate the post-clone sanitization checklist in a safe support/admin workflow.
