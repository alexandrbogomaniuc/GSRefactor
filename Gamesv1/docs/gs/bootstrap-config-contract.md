# GS Bootstrap Config Contract

Status: canonical source-of-truth for Gamesv1 bootstrap/runtime-config payloads.

## Ownership

- GS is authoritative for session, wallet, DB persistence, restore state, requestCounter, idempotency, routing, and config resolution.
- Browser is presentation-only and must not replace GS values with local financial/session truth.

## Bootstrap Response Shape (slot-browser-v1)

```json
{
  "session": {
    "sessionId": "string",
    "requestCounter": 10,
    "currentStateVersion": "string",
    "restore": {
      "hasUnfinishedRound": true,
      "resumeToken": "string"
    }
  },
  "wallet": {
    "balance": 12345,
    "currencyCode": "EUR",
    "truncCents": true,
    "delayedWalletMessages": false
  },
  "runtimeConfig": {
    "minBet": 10,
    "maxBet": 5000,
    "maxExposure": 100000,
    "defaultBet": 100,
    "betConfig": {
      "mode": "ladder",
      "betLadder": [10, 20, 50, 100],
      "coinValues": [0.01, 0.02, 0.05]
    },
    "capabilities": {
      "turbo": { "allowed": true, "speedId": "turbo-x2", "preferred": false },
      "animationPolicy": {
        "forcedSpinStopAllowed": true,
        "forcedSkipWinPresentation": true,
        "minReelSpinTimeMs": { "normal": 2000, "turbo": 1200 },
        "autoplayMinDelayMs": 250,
        "lowPerformanceMode": false
      }
    }
  },
  "presentation": {
    "localizedTitle": "Premium Slot",
    "notifications": [],
    "contentPath": "https://cdn.example.com/games/premium-slot/locales"
  }
}
```

## Required Runtime Config Families

- turbo / animation policy / min reel spin time / forced spin stop
- sound defaults + sound-toggle visibility
- localization policy: default language, missing localization error behavior, localized title
- content path customization + custom translation toggles
- spin profiling (`PRECSPINSTAT`)
- delayed wallet messages + wallet display policy
- history policy (url + same window)
- buy feature + buy feature disabled for cash bonus
- free spins / respin / Hold&Win / big-win tiers
- FRB / OFRB flags
- jackpot hooks
- session UI policy
- GL_DEFAULT_BET -> DEFCOIN fallback and max bet / max exposure resolution

## Client Rules

1. Hydrate `ResolvedRuntimeConfigStore` from bootstrap payload first.
2. Local defaults are dev fallback only when GS payload is missing.
3. Missing or unsupported config keys must be surfaced in dev logs.
4. Browser never treats local mock values as authoritative wallet/session state.
