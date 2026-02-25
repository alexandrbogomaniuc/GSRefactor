# Bank Template Policy (Singleplayer vs Multiplayer)

## Why this document exists
We cloned a new internal test bank (`6276`) from an older bank template and found many inherited settings that do not belong in a clean local/internal setup.

Most importantly:
- it inherited third-party internet URLs (`wallet.mqbase.com`)
- it inherited multiplayer/social/private-room settings that are not needed for singleplayer validation

This policy defines two clean bank templates so future banks are safer and easier to test.

## Simple rule (mandatory)
For local/internal banks:
- Disable all third-party internet URLs and domains.
- Keep only local/internal endpoints that are actually used in this environment.

## Template Types

### 1. Singleplayer Template (recommended default)
Use this when the goal is normal game launch, wallet flow, and singleplayer gameplay testing.

What stays enabled:
- Local wallet/BAV endpoints (`host.docker.internal:8000/bav/...`)
- Local MP transport only if the current launch shell still needs it (`MP_LOBBY_WS_URL`)
- Local support/error URLs (`localhost`)

What must be disabled or cleared:
- Friends/private-room integrations
- External winner-feed/jackpot feed integrations
- Any third-party internet URLs/domains (for example `mqbase.com`)

Minimum cleanup checklist (required)
- Set `ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM = false`
- Clear `UPDATE_PLAYER_STATUS_IN_PRIVATE_ROOM_URL`
- Clear `UPDATE_PLAYERS_ROOMS_NUMBER_URL`
- Clear `GET_FRIENDS_URL`
- Clear `INVATE_PLAYERS_TO_PRIVATE_ROOM_URL`
- Clear `GET_PLAYERS_ONLINE_STATUS_URL`
- Clear `NOTIFICATION_CLOSE_GAME_PROCESSOR_URL`
- Set `USE_WINNER_FEED = false`
- Set `NEEDS_JACKPOT3_FEED = false`
- Remove third-party domains from `ALLOWED_ORIGIN`
- Remove third-party domains from `ALLOWED_DOMAINS`

Verification checklist (required)
- Support page shows no third-party URLs in bank settings
- `/startgame` launch returns `200`
- GS logs show expected bank/subcasino routing
- If testing a singleplayer game, confirm `isMultiplayer=false` in logs

### 2. Multiplayer Template (use only when needed)
Use this when you are intentionally testing multiplayer/private-room/social features.

What stays enabled (only if feature is really used)
- MP-related settings and local MP endpoints
- Multiplayer/private-room/social URLs only when replaced with approved internal endpoints

What is still mandatory
- Third-party public internet URLs must still be disabled unless explicitly approved and documented
- `ALLOWED_ORIGIN` and `ALLOWED_DOMAINS` must only contain local/internal approved domains for test environments

Important note:
- “Multiplayer template” does not mean “copy all old legacy internet URLs.”
- It means “keep multiplayer features, but point them to approved internal services or disable them.”

## Evidence from current banks (what we observed)
- Banks `6274` and `6275` originally contained inherited third-party URLs (`wallet.mqbase.com`) and third-party domains in allow lists.
- In this cleanup pass, the same third-party URL/domain sanitization was applied to `6274`, `6275`, and `6276`.
- Post-cleanup launch smoke checks remained successful for all three banks (`HTTP 200`).

Sanitization evidence:
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/betonline-bank-6276-third-party-url-sanitization-20260225-180543.md`

## Why names like `com.dgphoenix.*`, `MQ*`, and `mqbase.com` still appear in some places
This is two different problems, and they should not be mixed:

1. Runtime config cleanup (safe now)
- URL/domain cleanup and feature disabling
- Bank template hygiene
- Local endpoint replacement

2. Runtime class/package rename (higher risk, later wave)
- Examples: `com.dgphoenix.*`, `MQBCloseGameProcessor`, `MQ_FRB_DEF_CHIPS`
- These values are often used by reflection, legacy processors, or persisted runtime config data
- Blind renaming can break runtime behavior

Current project status:
- The project completed tooling/governance for rename work (Phase 9 controlled waves)
- Broader runtime-sensitive renames were intentionally deferred to later approved waves
- There is no fully executed runtime-wide rename rollout yet

Plain English:
- We cleaned what is safe to clean now (URLs/domains/flags).
- We did not mass-rename runtime class names yet because that can break the server if done carelessly.

## Planned approach for name cleanup (recommended next)
Layer A (safe, now)
- Continue bank/template config cleanup
- Disable third-party URLs/domains
- Use local/internal endpoints only
- Document exceptions clearly

Layer B (later, controlled)
- Audit runtime string/class references that contain `com.dgphoenix` / `MQ`
- Add compatibility wrappers or migration mapping where needed
- Migrate in approved waves with runtime validation after each wave

## Operational Recommendation (short)
- Default every new internal bank to the Singleplayer Template.
- Upgrade to Multiplayer Template only when multiplayer/private-room testing is the actual goal.
