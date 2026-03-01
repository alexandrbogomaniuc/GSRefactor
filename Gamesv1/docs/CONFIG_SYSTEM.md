# CONFIG_SYSTEM

Production config resolution model for Gamesv1 Phase-1.

## Ownership

1. GS is authoritative for runtime limits and financial/state behavior.
2. Browser resolves and applies config for presentation/compliance behavior only.
3. Browser must not become source of truth for wallet/session/DB state.

## Layering (Highest Precedence First)

1. `launchParams` (bootstrap/launch values)
2. `currencyOverrides.<currency>`
3. `gameOverrides`
4. `bankProperties`
5. `templateDefaults`

Resolver input shape:
- `templateDefaults`
- `bankProperties`
- `gameOverrides`
- `currencyOverrides`
- `launchParams`

Implementation:
- `packages/core-compliance/src/ResolvedRuntimeConfig.ts`
- `packages/core-compliance/src/ConfigResolver.ts`
- `packages/core-compliance/src/CapabilityMatrix.ts`

## Resolved Runtime Contract

One object drives the shell:
- `ResolvedRuntimeConfig`

Primary families:
1. Betting/limits:
- `betConfig` (ladder or dynamic constraints)
- `minBet`, `maxBet`, `defaultBet`, `maxExposure`
2. Timing/animation:
- `turboplay`
- `minReelSpinTime`
- `capabilities.animationPolicy`
3. Localization:
- `localization.defaultLang`
- `localization.localizedTitleKey`
- `localization.contentPath`
- `localization.showMissingLocalizationError`
4. History behavior:
- `history.url`
- `history.openInSameWindow`
5. Feature/capability flags:
- buy feature, free spins/respin/Hold&Win, big win flow, delayed wallet messages, spin profiling, etc.

## Legacy Fallbacks

When `launchParams.defaultBet` is absent, resolver applies:
1. `GL_DEFAULT_BET`
2. `DEFCOIN`

These can also be provided under `launchParams.legacyDefaults`.

## Validation Rules

Resolver schema enforces:
1. `minBet <= maxBet`
2. `defaultBet` inside `minBet..maxBet`
3. `maxBet <= maxExposure`
4. `defaultBet <= maxExposure`
5. dynamic mode `maxStep <= maxExposure`
6. history URL cannot use `javascript:` scheme

## Dev Diff / Diagnostics

In dev mode, resolver emits:
1. per-layer override diff log
2. unsupported key warnings
3. legacy fallback warnings
4. missing currency override warnings

This is for troubleshooting only and does not alter GS authority model.
