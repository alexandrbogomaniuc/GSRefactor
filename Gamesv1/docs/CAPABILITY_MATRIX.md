# CAPABILITY_MATRIX

Executable capability matrix for Gamesv1.

Machine-readable source:
- `packages/core-compliance/src/CapabilityMatrix.ts`
- `packages/core-compliance/src/ResolvedRuntimeConfig.ts`
- `packages/core-compliance/src/ConfigResolver.ts`

Related GS contracts:
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-api-contract.md`

## Layer precedence

Highest precedence wins:
1. launch/bootstrap values
2. currency overrides
3. game overrides
4. bank properties
5. template defaults

## Capability families modeled

1. Turbo + animation policy + min reel spin time + forced spin stop
2. Sound defaults + sound mode by default + toggle visibility
3. Localization policy:
- default language
- localized title key/value
- missing localization error behavior
- content path + custom translation toggle
- server notifications toggle
4. Spin profiling (`PRECSPINSTAT`)
5. Wallet messaging policy (delayed/external)
6. Wallet display policy (balance/currency/delayed indicator)
7. History policy (enabled/url/same-window)
8. Runtime policies (requestCounter/idempotency/clientOperationId/currentStateVersion/restore)
9. Session UI policy (timer/reality-check/close-button)
10. Jackpot hooks policy
11. Feature flags:
- buy feature
- buy feature for cash bonus
- buy feature disabled for cash bonus
- free spins/respin/Hold&Win
- FRB/OFRB
- in-game history
- holiday/custom skins
12. Big/Huge/Mega win flow thresholds

## Legacy fallback behavior

Resolver fallback for `defaultBet` when `launchParams.defaultBet` is absent:
1. `GL_DEFAULT_BET` (or `legacyDefaults.GL_DEFAULT_BET`)
2. `DEFCOIN` (or `legacyDefaults.DEFCOIN`)

## Max bet / exposure rules

Validation enforces:
1. `minBet <= maxBet`
2. `defaultBet` in `[minBet, maxBet]`
3. `maxBet <= maxExposure`
4. `defaultBet <= maxExposure`
5. dynamic `maxStep <= maxExposure`

## Dev diagnostics

In dev mode resolver logs:
- layer override diff (`diffLog`)
- unsupported config/capability keys
- legacy fallback application warnings
- missing currency override warnings

## Runtime consumer contract

One resolved runtime config object drives the shell and HUD feature visibility.
