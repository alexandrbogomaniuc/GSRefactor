# Betonline Subcasino/Bank Expansion Validation (Internal Pre-Prod)

Date (UTC): 2026-02-25 17:10:54 UTC

## Purpose
Add one more test tenant so we can validate new-bank onboarding behavior and test more than the original two banks.

Requested by user:
- New subcasino name: `Betonline`
- New bank name: `betonline_test`
- Use next available IDs

## What Was Added
- New subcasino: `508` (`Betonline`)
- New internal bank: `6276` (`betonline_test`)

### Important compatibility note (simple English)
The wallet backend (Casino Side) still expects a numeric bank ID that it already knows.
Because of that, the new internal GS bank `6276` was configured to reuse external wallet bank ID `6274` for internal testing.

This means:
- GS/internal bank under the new subcasino is **still 6276**
- Launch URL must use `bankId=6274` together with `subCasinoId=508`
- `subCasinoId` is what makes this route go to the new bank (`6276`) instead of the original bank (`6274` in subcasino `507`)

## Execution Summary
1. Created subcasino `508` through GS support tool (`/support/createSubCasino.jsp`)
2. Cloned bank config + games from bank `6274` into new bank `6276` using `/support/copyBankV3.jsp`
3. Verified support pages show new subcasino and bank
4. Fixed wallet compatibility for internal testing by changing bank `6276` external bank ID from `betonline_test` to `6274` (display name remains `betonline_test`)
5. Validated game launch handoff on the new subcasino for two games (`838`, `829`)

## Results (Plain English)
- The new subcasino and bank were created successfully.
- The new bank launches games successfully after the external wallet bank ID compatibility adjustment.
- Tested launch pages show lobby/game asset references and websocket URL (same evidence pattern as previous successful launches).
- GS logs show the request is handled as internal bank `6276` under subcasino `508`.

## Tested Internal Launch URLs (working)
Dragonstone (`gameId=838`):
- `http://127.0.0.1:18080/startgame?bankId=6274&subCasinoId=508&gameId=838&mode=real&token=bav_game_session_001&lang=en`

Revenge of Ra (`gameId=829`):
- `http://127.0.0.1:18080/startgame?bankId=6274&subCasinoId=508&gameId=829&mode=real&token=bav_game_session_001&lang=en`

## Single-Player Logic Check (what we verified)
GS runtime logs for the new bank launch show:
- route reason `non_multiplayer_game`
- `isMultiplayer=false`

This confirms the tested path is not taking the multiplayer gameplay route for the checked game launch.

## Evidence Files
Support snapshots:
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/subcasino-508-support-snippet-20260225-171054.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/bank-6276-support-snippet-20260225-171054.txt`

Launch response evidence:
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/launch-508-6276-g838-http-20260225-171054.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/launch-508-6276-g838-markers-20260225-171054.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/launch-508-6276-g829-http-20260225-171054.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/launch-508-6276-g829-markers-20260225-171054.txt`

Runtime log evidence:
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/gs-betonline-launch-snippet-20260225-171054.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/casino-side-auth-snippet-20260225-171054.log`

## Known Limitation (for later go-live preparation)
To use a brand new external bank ID like `betonline_test` end-to-end, Casino Side must also support that bank (or the wallet API must support string IDs). That change was intentionally not done here because this is internal pre-production validation only.
