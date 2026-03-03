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
 - `GL_MAX_BET`, `exposureDerivedMaxBet`, final `maxBet=min(GL_MAX_BET, exposureDerivedMaxBet)`
2. Timing/animation:
- `turboplay`
- `minReelSpinTime`
- `capabilities.animationPolicy` + `animationPolicy` group (forced spin stop, spin profiling)
3. Localization:
- `localization.defaultLang`
- `localization.localizedTitleKey`
- `localization.localizedTitle`
- `localization.contentPath`
- `localization.showMissingLocalizationError`
 - `localization.serverNotificationsEnabled`
 - legacy alias mappings:
 - `USE_JP_NOTIFICATION` -> `localization.serverNotificationsEnabled`
 - `content_path` -> `localization.contentPath`
4. History behavior:
- `history.url`
- `history.openInSameWindow`
5. Feature/capability flags:
- buy feature, buy feature disabled for cash bonus, free spins/respin/Hold&Win
- FRB/OFRB
- jackpot hooks
- delayed wallet messages
- spin profiling (`PRECSPINSTAT`)
- session UI policy
- big/huge/mega win flow thresholds
 - legacy alias mappings:
 - `spinProfilingEnabled` / `PRECSPINSTAT` -> `animationPolicy.spinProfilingEnabled`
 - `delayedWalletMessages` -> `walletDisplayPolicy.delayedWalletMessages`
 - `BUY_FEATURE_DISABLED_FOR_CASH_BONUS` -> `featurePolicy.buyFeatureDisabledForCashBonus`
 - `FRB` / `OFRB` -> `featurePolicy.frb` / `featurePolicy.ofrb`
 - `jackpotHooksEnabled` -> `featurePolicy.jackpotHooksEnabled`

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
3. legacy alias mapping notes
4. legacy fallback warnings
5. missing currency override warnings

This is for troubleshooting only and does not alter GS authority model.
