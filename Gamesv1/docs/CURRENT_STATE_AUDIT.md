# CURRENT_STATE_AUDIT

Phase-1 architecture alignment audit based on the current Gamesv1 codebase.

Date: 2026-02-28
Branch: `main`

## 1) Canon Alignment Check

Status: PASS

- Canonical runtime target is GS HTTP in:
  - `docs/MasterContext.md`
  - `docs/PROJECT.md`
  - `docs/protocol/extgame.md`
  - `.agent/rules/01_rules_protocol.md`
- WebSocket path is explicitly marked legacy/experimental in:
  - `docs/protocol/abs-gs-v1.md`
  - `docs/RELEASE_PROCESS.md`
  - `.agent/rules/agent_rules.md`
- Browser state ownership is presentation-only; GS owns session/wallet/DB/restore/requestCounter/idempotency in canonical docs.

## 2) Capability Matrix Coverage

Status: PASS

Implemented in:
- `packages/core-compliance/src/CapabilityMatrix.ts`
- `packages/core-compliance/src/ResolvedRuntimeConfig.ts`
- `packages/core-compliance/src/ConfigResolver.ts`

Covered families:
1. Turbo / min spin time / forced spin stop
2. Sound defaults + sound toggle visibility
3. Localization policies + `contentPath` + localization error behavior + `localizedTitleKey`
4. Spin profiling (`PRECSPINSTAT`)
5. Delayed wallet/external wallet messaging flags
6. Buy feature + buy feature for cash bonus
7. History URL + same-window rule (`history.openInSameWindow`)
8. Free spins / respin / Hold&Win / big-huge-mega win flow
9. Legacy fallback order: `GL_DEFAULT_BET` -> `DEFCOIN`
10. Max bet/exposure rules (`maxBet <= maxExposure`, `defaultBet <= maxExposure`)

Validation coverage:
- `tests/compliance/config.test.ts`
- `tests/compliance/fixtures/config-layers/*.json`

## 3) Transport Model Check

Status: PASS (with documented legacy adapters)

- Canonical browser transport model: browser -> GS only.
- No browser -> slot-engine direct communication modeled in canonical flow.
- Internal slot-engine/RNG/audit treated as server-side only in:
  - `docs/MasterContext.md`
  - `docs/PROJECT.md`
  - `docs/protocol/extgame.md`
  - `.agent/workflows/release_game.md`

Core transport package notes:
- `packages/core-protocol/src/http/ExtGameTransport.ts`: GS HTTP runtime transport.
- `packages/core-protocol/src/index.ts`: supports canonical `HTTP_GS`/`EXTGAME`; retains `WS`/`WS_LEGACY`.
- `packages/core-protocol/src/ws/GsWsTransport.ts`: legacy adapter retained for experimental/backward compatibility.

## 4) Release Artifact Pipeline Check

Status: PASS

Pipeline implementation:
- `tools/release-pack/create-release.ts`
- `package.json` script: `release:pack`

Output set produced by command:
- `npm run release:pack -- --game premium-slot --static-origin https://cdn.example.com/games --skip-build`

Required artifacts generated:
- client bundle manifest
- asset manifest
- localization manifest
- math package manifest reference
- hashes/checksums
- GS registration pack
- rollback pack
- canary checklist
- smoke-test checklist

Reference docs:
- `docs/RELEASE_ARTIFACTS.md`
- `docs/GS_REGISTRATION_ARTIFACTS.md`

## 5) Legacy/Experimental WS Inventory

These are intentionally retained but non-canonical:

1. `packages/core-protocol/src/ws/GsWsTransport.ts`
2. `packages/core-protocol/src/schemas.ts` (`AbsEnvelopeSchema`)
3. `tests/contract/mock-gs/*` (WS mock server + runner)
4. `tests/contract/transport.contract.test.ts` (includes WS transport assertions)
5. `docs/protocol/abs-gs-v1.md` (deprecated protocol doc)

Policy:
- Keep as legacy/experimental compatibility surface only.
- Do not use for new production integrations.

## 6) Canonical Create-Game Path

Status: PASS

- Canonical command:
  - `npm run create-game -- --gameId <gameId> --name "<name>" --themeId <themeId> --languages en,es,de`
- Canonical workflow:
  - `.agent/workflows/new_game.md`
- Scaffold implementation:
  - `tools/create-game.ts`

## 7) Build/Test Summary

Executed and passing:
1. `corepack pnpm build` -> PASS
2. `npm run test:config` -> PASS
3. `npm run test:animation-policy` -> PASS
4. `npm run test:layout` -> PASS
5. `npm run test:contract` -> PASS
6. `npm run release:pack -- --game premium-slot --static-origin https://cdn.example.com/games --skip-build` -> PASS

## 8) Open Risks

1. Legacy WS adapter remains in `core-protocol` and contract tests; must stay clearly labeled non-canonical.
2. `tools/create-game/` subpackage exists alongside canonical `tools/create-game.ts`; only `npm run create-game` should be used operationally.
