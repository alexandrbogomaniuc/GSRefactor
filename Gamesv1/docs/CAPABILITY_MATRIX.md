# CAPABILITY_MATRIX

Executable capability matrix for Gamesv1.

This document mirrors the machine-readable model in:
- `packages/core-compliance/src/CapabilityMatrix.ts`
- `packages/core-compliance/src/ResolvedRuntimeConfig.ts`
- `packages/core-compliance/src/ConfigResolver.ts`

## Purpose

`GAME_CLIENT_REQUIREMENTS_MAIN` defines behavioral requirements.
The capability matrix turns those requirements into runtime-resolved config consumed by the client shell.

## Layer Precedence

Highest precedence wins:

1. `launch/bootstrap values`
2. `currency overrides`
3. `game overrides`
4. `bank properties`
5. `template defaults`

Resolver input shape:
- `templateDefaults`
- `bankProperties`
- `gameOverrides`
- `currencyOverrides`
- `launchParams`

## Capability Families

## 1) Turbo / Animation / Min Reel Spin

Path:
- `capabilities.turbo`
- `capabilities.animationPolicy`
- mirrored root fields: `turboplay`, `minReelSpinTime`

Fields:
- `turbo.allowed`
- `turbo.speedId`
- `turbo.preferred`
- `animationPolicy.forcedSpinStopAllowed`
- `animationPolicy.forcedSkipWinPresentation`
- `animationPolicy.minReelSpinTimeMs.normal`
- `animationPolicy.minReelSpinTimeMs.turbo`
- `animationPolicy.autoplayMinDelayMs`

## 2) Sound Defaults / Toggle Visibility

Path:
- `capabilities.sound`
- mirrored root field: `soundDefaults`

Fields:
- `enabledByDefault`
- `showToggle`
- `masterVolume`
- `bgmVolume`
- `sfxVolume`

## 3) Localization / Content Path

Path:
- `capabilities.localization`
- mirrored root field: `localization`

Fields:
- `defaultLanguage`
- `localizedTitleKey`
- `showMissingLocalizationError`
- `contentPath`
- `customTranslationsEnabled`

## 4) Forced Spin Stop

Path:
- `capabilities.animationPolicy.forcedSpinStopAllowed`

## 5) Spin Profiling / PRECSPINSTAT

Path:
- `capabilities.spinProfiling`

Fields:
- `enabled`
- `payloadKey` (fixed: `PRECSPINSTAT`)

## 6) Delayed Wallet / External Wallet Messages

Path:
- `capabilities.walletMessaging`

Fields:
- `delayedWalletMessages`
- `externalWalletMessages`

## 7) Buy Feature / Cash Bonus Buy

Path:
- `capabilities.features`

Fields:
- `buyFeature`
- `buyFeatureForCashBonus`

## 8) Free Spins / Respin / Hold&Win / Big Win Rules

Path:
- `capabilities.features`
- `capabilities.bigWinFlow`

Fields:
- `freeSpins`
- `respin`
- `holdAndWin`
- `bigWinFlow.enabled`
- `bigWinFlow.allowSkipPresentation`
- `bigWinFlow.thresholds.bigMultiplier`
- `bigWinFlow.thresholds.hugeMultiplier`
- `bigWinFlow.thresholds.megaMultiplier`

## 9) In-Game History + Launch Rules

Path:
- `capabilities.features.inGameHistory`
- `capabilities.history`

Fields:
- `history.enabled`
- `history.url`
- `history.openInSameWindow`

## 10) Holiday Mode / Optional Skin Flags

Path:
- `capabilities.features`

Fields:
- `holidayMode`
- `customSkins`

## 11) Bootstrap Runtime Policy Group

Path:
- `capabilities.runtimePolicies`
- mirrored root field: `runtimePolicies`

Fields:
- `requestCounterRequired`
- `idempotencyKeyRequired`
- `clientOperationIdRequired`
- `currentStateVersionSupported`
- `unfinishedRoundRestoreSupported`

## Legacy Fallbacks

Resolver supports launch legacy fallback values for default bet:

- `GL_DEFAULT_BET`
- `DEFCOIN`
- `legacyDefaults.GL_DEFAULT_BET`
- `legacyDefaults.DEFCOIN`

Fallback order for `defaultBet` when `launchParams.defaultBet` is absent:
1. `GL_DEFAULT_BET` (or `legacyDefaults.GL_DEFAULT_BET`)
2. `DEFCOIN` (or `legacyDefaults.DEFCOIN`)

## Max Bet / Exposure Resolution Rules

Resolved config validation enforces:
1. `minBet <= maxBet`
2. `defaultBet` must be inside `minBet..maxBet`
3. `maxBet <= maxExposure`
4. `defaultBet <= maxExposure`
5. For dynamic bet mode, `dynamicBetConstraints.maxStep <= maxExposure`
6. History URL must not use `javascript:` scheme

## Dev Diff + Warning Logs

In dev mode, resolver logs:
1. value override diff by layer and key
2. unsupported config/capability keys
3. legacy fallback application notices
4. missing currency override notices

## Runtime Consumer Contract

One object drives shell behavior:
- `ResolvedRuntimeConfig`

Current consumer example:
- `games/premium-slot/src/app/screens/main/MainScreen.ts`
  - HUD visibility reads capability flags (turbo/autoplay/buy feature/sound toggle)

