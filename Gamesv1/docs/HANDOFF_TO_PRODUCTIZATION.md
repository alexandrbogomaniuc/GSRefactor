# HANDOFF_TO_PRODUCTIZATION

Current gate state: **NOT GREEN** (see `docs/PRODUCTIZATION_GATE.md`).
Productization sprint should not start until gate blockers are resolved.

## Next sprint should build

1. Premium template HUD polish
- Final control behaviors for spin/turbo/autoplay/buy/sound/settings/history.
- Responsive panel behavior without empty gaps for disabled controls.

2. Presentation layer polish
- High-quality animation/VFX pass driven strictly by browser-visible `presentationPayload`.
- Sound and UI feedback polish aligned with resolved runtime config/capabilities.

3. Art and content integration
- Asset and localization production handoff using canonical pipeline docs.
- Finalized per-game manifests and budget enforcement.

4. Release-ready quality pass
- End-to-end release pack validation on candidate games.
- Launch/reconnect/history/free-spins/buy-feature smoke coverage in CI.

## What must remain frozen

- Canonical runtime contract interpretation from `docs/gs/*`.
- `/slot/v1/*` endpoint naming and wire field naming.
- Bootstrap/runtime state ownership model (GS authoritative).
- Release-registration schema and artifact naming contract.
- Canonical scaffolder folder/file conventions.

## Canonical docs

- Architecture/context: `docs/MasterContext.md`, `docs/PROJECT.md`
- Runtime/release contracts: `docs/gs/*`
- Capability spec: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- Release artifacts: `docs/RELEASE_ARTIFACTS.md`, `docs/GS_REGISTRATION_ARTIFACTS.md`
- Readiness controls: `docs/PHASE1_PREPARATION_CLOSEOUT.md`, `docs/PRODUCTIZATION_GATE.md`

## Legacy/optional folders and scope

- Legacy WS transport: `packages/core-protocol/src/ws/*` (non-canonical).
- Legacy protocol notes: `docs/protocol/*` and archived protocol docs.
- Operator-pariplay package: `packages/operator-pariplay/*` (optional/out-of-scope for canonical GS phase-1 runtime).
