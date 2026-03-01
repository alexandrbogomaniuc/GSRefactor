# Docs Map

Canonical and archived documentation index.

## Canonical docs

- `docs/MasterContext.md`: architecture canon and ownership boundaries.
- `docs/PROJECT.md`: project charter for locked GS architecture.
- `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`: client capability/behavior spec.
- `docs/CAPABILITY_MATRIX.md`: executable capability matrix mapping.
- `docs/CONFIG_SYSTEM.md`: layered config and resolver behavior.
- `docs/RELEASE_PROCESS.md`: release-manager execution checklist.

## Canonical GS contracts (`docs/gs/`)

- `docs/gs/bootstrap-config-contract.md`: bootstrap payload and config ownership.
- `docs/gs/browser-runtime-api-contract.md`: browser runtime endpoint contract.
- `docs/gs/browser-error-codes.md`: browser-facing runtime error classes.
- `docs/gs/browser-runtime-sequence-diagrams.md`: end-to-end runtime sequences.
- `docs/gs/release-registration-contract.md`: required release artifact contracts.
- `docs/gs/enable-disable-canary-rollback.md`: operational rollout/rollback contract.

## Protocol notes

- `docs/protocol/gs-http-runtime.md`: compatibility pointer to canonical `docs/gs/*` contracts.
- `docs/protocol/browser-runtime-api-contract.md`: compatibility pointer to canonical `docs/gs/*` contracts.
- `docs/protocol/abs-gs-v1.md`: legacy/experimental only.

## Source of truth by concern

- Architecture: `docs/MasterContext.md`, `docs/PROJECT.md`
- Runtime API: `docs/gs/browser-runtime-api-contract.md`
- Bootstrap config: `docs/gs/bootstrap-config-contract.md`
- Capabilities/config: `docs/CAPABILITY_MATRIX.md`, `docs/CONFIG_SYSTEM.md`
- Release/registration: `docs/RELEASE_PROCESS.md`, `docs/gs/release-registration-contract.md`
- Canary/rollback: `docs/gs/enable-disable-canary-rollback.md`
- Agent protocol rules: `.agent/rules/01_rules_protocol.md`
- Premium reference architecture: `games/premium-slot/docs/ARCHITECTURE_MAP.md`

## Archived and deprecated

- Archived docs live under `docs/_archive/` with warning headers.
- Legacy WebSocket protocol docs are retained but non-canonical.
- `/v1/placebet` and `/v1/collect` browser contract assumptions are deprecated and replaced by `/v1/playround`.
- Legacy WS/operator tests are archived under `tests/_archive/`.

## Contradiction prevention rule

When behavior changes:
1. update `docs/gs/*` first,
2. update core canon docs,
3. archive/deprecate older conflicting docs.
