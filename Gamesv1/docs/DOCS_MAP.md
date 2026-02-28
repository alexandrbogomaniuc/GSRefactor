# Docs Map

Index of canonical and archived/deprecated documentation.

## Canonical Docs

- `docs/MasterContext.md`: canonical architecture context and runtime ownership.
- `docs/PROJECT.md`: project charter and target architecture.
- `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`: client capability/behavior spec.
- `docs/RELEASE_PROCESS.md`: release packaging and deployment checklist.
- `docs/RELEASE_ARTIFACTS.md`: deterministic release-pack artifact contract.
- `docs/GS_REGISTRATION_ARTIFACTS.md`: GS Ops registration/enable/rollback artifact usage.
- `docs/CONFIG_SYSTEM.md`: config layering and runtime resolution.
- `docs/LOCALIZATION.md`: localization architecture and validation.
- `docs/ART_AND_PROMO_PIPELINE.md`: art deliverables, export specs, quality gates.
- `docs/ASSET_MANIFEST_SPEC.md`: per-game art manifest schema.
- `docs/protocol/extgame.md`: canonical GS HTTP runtime transport path.

## Deprecated (Still Present)

- `docs/protocol/abs-gs-v1.md`: legacy/experimental only; not canonical production path.

## Archived Docs

All archived docs are in `docs/_archive/` and include a warning header.

Current archived set:
- `docs/_archive/ActivityLog.md`
- `docs/_archive/ARCHITECTURE_TREE.md`
- `docs/_archive/ASSET_PIPELINE.md`
- `docs/_archive/GSDatabaseRegistrationGuide.md`
- `docs/_archive/Independent_Development_Guide.md`
- `docs/_archive/MasterContext.md`
- `docs/_archive/Milestones.md`
- `docs/_archive/NFR.md`
- `docs/_archive/PERF_BUDGET.md`
- `docs/_archive/PixiJS_Initialization_Guide.md`
- `docs/_archive/PRD.md`
- `docs/_archive/PROJECT.md`
- `docs/_archive/SprintPlan.md`
- `docs/_archive/TemplateIntegrationGuide.md`
- `docs/_archive/VFX_LIBRARY.md`
- `docs/_archive/WowFxRecommendations.md`

## How To Update Docs Without Causing Contradictions

1. Update `docs/MasterContext.md` first for architecture decisions.
2. Update only impacted canonical docs listed above.
3. If a doc is superseded, move it to `docs/_archive/` with warning header.
4. If a doc is legacy but intentionally kept, mark it "Deprecated" in this map.
5. `.agent` rules/workflows must mirror canonical docs; they must not introduce alternate architecture truth.
