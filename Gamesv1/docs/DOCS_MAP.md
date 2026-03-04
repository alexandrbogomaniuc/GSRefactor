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
- `docs/SHARED_HUD_ARCHITECTURE.md`: reusable premium HUD/control panel architecture.
- `docs/HUD_CONFIG_SCHEMA.md`: HUD visibility/config schema and capability mapping.
- `docs/PRESENTATION_LAYER_ARCHITECTURE.md`: canonical payload-to-UI mapping architecture.
- `docs/FEATURE_MODULE_SYSTEM.md`: pluggable feature-module system for template reuse.
- `docs/RESPONSIVE_SCALING_STRATEGY.md`: shared scaling strategy and layout matrix expectations.
- `docs/SAFE_AREA_POLICY.md`: safe-area/notch handling policy for all game shells.
- `docs/WOW_VFX_ARCHITECTURE.md`: shared win/VFX orchestration architecture.
- `docs/THEME_SHELL_FOUNDATION.md`: shell token foundation (HUD/VFX/audio/action/win-target hooks).
- `docs/HUD_THEME_TOKENS.md`: HUD-specific theme token schema and integration points.
- `docs/WIN_PRESENTATION_TIERS.md`: canonical win tier mapping and timing rules.
- `docs/ART_PIPELINE_PRODUCTIZATION.md`: reusable art integration contract for new games.
- `docs/PROMO_ASSET_PIPELINE.md`: promo asset flow and release handoff policy.
- `docs/VIDEO_OVERLAY_GUIDELINES.md`: video overlay integration and fallback rules.
- `docs/PERFORMANCE_GUARDRAILS.md`: frontend performance guardrails for productization.
- `docs/CODE_SPLITTING_PLAN.md`: concrete chunking mitigation and follow-up plan.

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
