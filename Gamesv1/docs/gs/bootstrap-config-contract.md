# GS Bootstrap Config Contract for New Slots

- Status: Draft for implementation (v1)
- Date: 2026-02-28
- Scope: GS -> Browser Client launch/bootstrap payload
- Related:
  - `docs/adr/0001-new-slots-target-architecture.md`
  - `docs/09-game-client-requirements-checklist.md`
  - `docs/gs/math-package-spec.md`

## 1) Purpose

Define exactly what GS must provide to the client at launch/bootstrap so there is no ambiguity about ownership, capability resolution, and runtime version pins.

## 2) Ownership Boundary (Normative)

- GS is authoritative for:
  - session identity and validation,
  - bank/game/currency/lang/mode context,
  - resolved capability flags and config values,
  - math package/version and RTP model selection,
  - selected client package/version,
  - route decision (legacy vs new slot path).
- Browser client is authoritative only for rendering and user input handling.
- Slot-engine host is limited to loading/executing only GS-selected, pre-registered verified math package versions.

## 3) Delivery Phases

### 3.1 Launch Redirect Payload

Returned by GS launch route (for example `/cwstartgamev2.do`):
- minimal routing fields needed to load client shell.

### 3.2 Bootstrap API Payload

Returned only by browser bootstrap endpoint:
- `POST /slot/v1/bootstrap`
- full resolved runtime contract used by shell and game UI.

Bootstrap request semantics:
- read-only call; it does not advance gameplay state.
- does not increment `requestCounter`.
- does not require `idempotencyKey`.
- must not be treated as an `openGame` substitute.
- in-game history URL exposed to browser policy must resolve to `POST /slot/v1/gethistory`.

## 4) Required Fields (Launch + Bootstrap)

GS MUST provide the following fields to client bootstrap.

```json
{
  "contractVersion": "slot-bootstrap-v1",
  "session": {
    "sessionId": "SID-123",
    "requestCounter": 1,
    "stateVersion": 0
  },
  "context": {
    "bankId": 6274,
    "subCasinoId": 507,
    "gameId": "10045",
    "currencyCode": "USD",
    "lang": "en",
    "mode": "real"
  },
  "assets": {
    "assetBaseUrl": "https://cdn.example.com/slots/10045/2.6.0/",
    "clientVersion": "2.6.0",
    "clientPackageVersion": "client-pkg-2.6.0",
    "assetBundleHash": "sha256:abcd..."
  },
  "runtime": {
    "mathPackageVersion": "1.4.2",
    "rtpModelId": "base-96.20",
    "engineContractVersion": "slot-runtime-v1"
  },
  "policies": {
    "animationPolicy": {
      "turboEnabled": true,
      "forcedSpinStopEnabled": true,
      "minSpinTimeMs": 1200,
      "spinProfilingEnabled": false,
      "spinProfilingSourceKey": "PRECSPINSTAT"
    },
    "soundPolicy": {
      "soundEnabledByDefault": true,
      "soundModeByDefault": "ON",
      "soundModeSourceKey": "SOUND_MODE_BY_DEFAULT"
    },
    "localizationPolicy": {
      "defaultLanguage": "en",
      "localizationOverridesEnabled": true,
      "showGameLocalizationError": true,
      "showGameLocalizationErrorSourceKey": "SHOW_GAME_LOCALIZATION_ERROR",
      "localizeGameTitle": true,
      "localizeGameTitleSourceKey": "LOCALIZE_GAME_TITLE",
      "contentPath": "https://cdn.example.com/content/slots/10045/",
      "contentPathSourceKey": "CUSTOMER_SETTINGS_URL",
      "contentPathLegacyAliases": [
        "content_path"
      ],
      "customTranslationsEnabled": true,
      "serverNotificationsEnabled": true,
      "serverNotificationsSourceKey": "USE_JP_NOTIFICATION"
    },
    "historyPolicy": {
      "enableInGameHistory": true,
      "gameHistoryUrl": "/slot/v1/gethistory",
      "openGameHistoryInSameWindow": true,
      "sourceKeys": [
        "ENABLE_IN_GAME_HISTORY",
        "GAME_HISTORY_URL",
        "OPEN_GAME_HISTORY_IN_SAME_WINDOW"
      ]
    },
    "walletDisplayPolicy": {
      "truncateCents": false,
      "currencyPrefix": "$",
      "currencySuffix": "",
      "decimalSeparator": ".",
      "groupingSeparator": ",",
      "minorUnitPrecision": 2,
      "delayedWalletMessages": true
    },
    "featurePolicy": {
      "buyFeatureEnabled": true,
      "buyFeatureDisabledForCashBonus": true,
      "buyFeatureDisabledForCashBonusSourceKey": "BUY_FEATURE_DISABLED_FOR_CASH_BONUS",
      "frbEnabled": true,
      "ofrbEnabled": true,
      "jackpotHooksEnabled": true,
      "freeSpinsEnabled": true,
      "respinEnabled": true,
      "holdAndWinEnabled": true,
      "bigWinEnabled": true,
      "megaWinEnabled": true
    },
    "sessionUiPolicy": {
      "showSessionDuration": true,
      "showSessionLossLimit": false,
      "showSessionBetWinTotals": true
    }
  },
  "uiPolicy": {
    "homeUrl": "https://operator.example/home",
    "cashierUrl": "https://operator.example/cashier",
    "launchHomeFromIframe": false
  },
  "integrity": {
    "configIssuedAtUtc": "2026-02-28T20:00:00Z",
    "configId": "cfg-8f42",
    "configHash": "sha256:1234..."
  }
}
```

## 4.1 Canonical Field Types (Normative)

These types are mandatory across bootstrap, browser runtime API, and internal runtime contracts.

- `requestCounter`: JSON integer (`number`), non-negative, max `9007199254740991`.
- `stateVersion`: JSON integer (`number`), non-negative, max `9007199254740991`.
- `currentStateVersion`: JSON integer (`number`), non-negative, max `9007199254740991`.
- Balance/bet minor-unit fields (for example `balanceMinor`, `betMinor`, `totalBetMinor`, `coinValueMinor`, `winMinor`, `netEffectMinor`): JSON integer (`number`) in minor currency units; no floating-point values.

Rules:
- These fields must never be encoded as strings in one contract and numbers in another.
- Any contract evolution that changes these wire types requires a new contract version.

## 5) Capability Resolution Rules

Values in `policies` MUST be resolved by GS before sending to browser using bank/game/template policy and released package support.

Precedence:
1. Regulatory/jurisdiction hard requirements.
2. Bank-level overrides.
3. Game-level defaults.
4. Client default fallback (only when explicitly allowed by GS contract).

If GS disables a capability/policy flag, client must not enable it locally.

## 5.1 Config Resolution Hooks (Normative)

GS config resolution must preserve these legacy behavioral rules:

1. `GL_DEFAULT_BET` fallback to `DEFCOIN` when explicit game/currency override is absent.
2. Override priority is game+currency > game-only > bank defaults > template defaults.
3. Final maximum bet must resolve as:
   - `finalMaxBet = min(GL_MAX_BET, exposureDerivedMaxBet)`.

## 6) Immutable Version Pinning Rules

- `clientVersion` and `clientPackageVersion` are immutable for active session.
- `mathPackageVersion` is immutable for active session.
- Production launch must never accept runtime-provided overrides for these versions.
- New versions require new release registration in GS and a new launch/bootstrap cycle.

## 7) Production Prohibitions

The bootstrap contract must not allow:

1. Launch-time math model injection in production.
2. Client-side switching of math package version.
3. Client-side financial authority or wallet commit decisions.

## 8) Resume and Reconnect Behavior

On reconnect:
- GS returns authoritative `session.requestCounter` and `session.stateVersion`.
- GS includes unfinished-round context indicators (when present).
- Client must reconcile to GS values and discard stale local assumptions.
- Client must treat policy groups as authoritative session config and avoid stale local caches.

## 9) Backward Compatibility Strategy

- `contractVersion` controls schema evolution.
- New optional fields may be added without breaking older clients.
- Removing/renaming required fields requires new contract version and client rollout gate.

## 10) Validation Checklist

Before enabling a game in production:

1. Verify all required fields are present and non-empty.
2. Verify asset hash/version match released bundle.
3. Verify math package version is registered and immutable in session.
4. Verify policy map matches approved bank/game policy.
5. Verify policy groups include required keys:
   - `SOUND_MODE_BY_DEFAULT`
   - forced spin stop
   - `PRECSPINSTAT`
   - `ENABLE_IN_GAME_HISTORY`
   - `GAME_HISTORY_URL`
   - `OPEN_GAME_HISTORY_IN_SAME_WINDOW`
   - `CUSTOMER_SETTINGS_URL` (legacy alias `content_path`) for content path resolution
   - `SHOW_GAME_LOCALIZATION_ERROR`
   - `LOCALIZE_GAME_TITLE`
   - `BUY_FEATURE_DISABLED_FOR_CASH_BONUS`
6. Verify reconnect returns authoritative counter/state version.
7. Verify legacy route fallback remains unaffected.
