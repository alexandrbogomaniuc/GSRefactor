# UPSTREAM_EXACTNESS_REPORT

Date: 2026-03-03
Scope: runtime transport exactness for GS slot-browser-v1

## Bootstrap exactness

Fixed and verified:
1. Bootstrap request/response modeled as dedicated bootstrap contract (not runtime envelope).
2. `GsRuntimeClient` bootstrap flow hydrates bootstrap config/policy state separately from mutating runtime state.
3. `BootstrapConfigStore` and `SessionRuntimeStore` are split by responsibility.

## gethistory exactness

Fixed and verified:
1. `gethistory` uses canonical request shape (`historyQuery` object).
2. `gethistory` is enforced as read-only.
3. `gethistory` does not advance runtime state or request counter.

## playround wire exactness

Fixed and verified:
1. Canonical `selectedBet` shape enforced in transport/tests:
- `coinValueMinor`
- `lines`
- `multiplier`
- `totalBetMinor`
2. Contract tests validate duplicate/idempotent playround semantics.

## Canonical transport surface

- Canonical operations only: `bootstrap`, `opengame`, `playround`, `featureaction`, `resumegame`, `closegame`, `gethistory`
- Transitional aliases removed from canonical interface surface
- WS adapter retained only as legacy/experimental scope

## Release-registration exactness

- `tools/release-pack/create-release.ts` now emits `release-registration.json` that validates against upstream schema.
- Removed non-canonical top-level fixture wrapper carry-through (`fixtureVersion`, `name`) from generated registration payload.

## Verification summary

- `corepack pnpm run test:contract`: PASS (10/10)
- `corepack pnpm run test`: PASS
- `corepack pnpm run build`: PASS
- `corepack pnpm run release:pack -- --game premium-slot`: PASS

## Legacy/experimental outside canonical scope

- `packages/core-protocol/src/ws/*`
- archived protocol materials under `docs/_archive/*` and legacy protocol docs marked non-canonical
