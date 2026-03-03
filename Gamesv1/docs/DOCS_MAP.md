# DOCS_MAP

Canonical and archived documentation index.

## Canonical Docs

- `docs/MasterContext.md`: architecture canon and authority boundaries.
- `docs/PROJECT.md`: project charter for locked GS architecture.
- `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`: client capability/behavior spec.
- `docs/CAPABILITY_MATRIX.md`: capability matrix and mapping notes.
- `docs/CONFIG_SYSTEM.md`: layered config/resolution behavior.
- `docs/RELEASE_PROCESS.md`: release-manager runbook.
- `docs/PHASE1_GOLDEN_PATH.md`: canonical runtime happy-path sequence for phase-1.
- `docs/UPSTREAM_CONTRACT_SYNC.md`: upstream sync process for canonical GS pack mirror.
- `docs/PHASE1_PREPARATION_CLOSEOUT.md`: prep sprint closeout status.
- `docs/PRODUCTIZATION_GATE.md`: explicit gate conditions before polish/productization.
- `docs/HANDOFF_TO_PRODUCTIZATION.md`: frozen boundaries and next-sprint handoff.

## Canonical GS Contract Pack

- `docs/gs/README.md`: canonical entry point for runtime/release contracts.
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/browser-error-codes.md`
- `docs/gs/browser-runtime-sequence-diagrams.md`
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`
- `docs/gs/fixtures/*`
- `docs/gs/schemas/*`

## Canonical Endpoint Naming

- Canonical prefix: `/slot/v1/*`
- Canonical history endpoint: `/slot/v1/gethistory`
- `/v1/*` canonical endpoint interpretation is obsolete.

## Legacy / Archived Protocol Docs

- `docs/protocol/abs-gs-v1.md`: archived/legacy marker.
- `docs/protocol/extgame.md`: archived/legacy marker.
- `docs/protocol/browser-runtime-api-contract.md`: compatibility pointer only.
- `docs/protocol/gs-http-runtime.md`: compatibility pointer only.
- `docs/_archive/protocol/abs-gs-v1.md`: archived legacy reference.
- `docs/_archive/protocol/extgame.md`: archived legacy reference.

## Optional / Non-Canonical Code Scope

- `packages/operator-pariplay/*`: optional/out-of-scope for canonical GS runtime path.
- `packages/core-protocol/src/ws/*`: legacy/experimental transport only.

## Source Of Truth By Concern

- Client capabilities: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- Runtime/release contracts: `docs/gs/*`
- Repo-generated schema helpers: `docs/generated/gs-contract/*` (non-canonical)
- Architecture canon: `docs/MasterContext.md`, `docs/PROJECT.md`
- Release execution: `docs/RELEASE_PROCESS.md`
- Agent protocol constraints: `.agent/rules/01_rules_protocol.md`

## Contradiction Prevention Rule

When behavior changes:
1. update `docs/gs/*` first,
2. update canon docs,
3. archive or deprecate conflicting docs under `docs/_archive/` or explicit legacy markers.
