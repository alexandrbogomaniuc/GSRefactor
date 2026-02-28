# Docs Map

Index of canonical and archived documentation.

## Canonical Docs

- `docs/MasterContext.md`: single canonical project context and boundaries.
- `docs/RELEASE_PROCESS.md`: release manager and agent release checklist.
- `docs/CONFIG_SYSTEM.md`: runtime config layering and generation flow.
- `docs/LOCALIZATION.md`: translation architecture and validation rules.
- `docs/ART_AND_PROMO_PIPELINE.md`: art deliverables, export specs, quality gates.
- `docs/ASSET_MANIFEST_SPEC.md`: per-game art manifest schema and validation rules.
- `docs/protocol/abs-gs-v1.md`: websocket protocol expectations.
- `docs/protocol/extgame.md`: extgame HTTP transport behavior.
- `docs/protocol/spin-profiling.md`: spin profiling contract.
- `docs/compliance/client-requirements-checklist.md`: compliance checklist.
- `docs/compliance/config-resolution.md`: config resolution behavior.
- `docs/game/round-lifecycle.md`: round lifecycle reference.
- `docs/qa/AAA_quality_gate.md`: QA quality gate.
- `docs/qa/bug-template.md`: bug report template.

## Archived Docs

All archived/outdated guides are under `docs/_archive/` and include a warning header.

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

Short rule:
1. Edit `docs/MasterContext.md` first when a rule or boundary changes.
2. Update only the affected canonical guide(s) in this map.
3. If an old guide conflicts, move it to `docs/_archive/` and prepend the archive warning header.
4. Do not keep duplicate guidance in both `.agent/context.md` and docs; `.agent/context.md` must point to `docs/MasterContext.md`.
