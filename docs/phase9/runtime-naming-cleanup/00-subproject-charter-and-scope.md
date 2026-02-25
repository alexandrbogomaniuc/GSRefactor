# Subproject Charter: Runtime Naming Cleanup (Phase 9 Follow-On)

Date (UTC): 2026-02-25 18:27:26 UTC

## Plain-English Summary
The main modernization project is now technically ready for deploy/canary, but one important cleanup area is still unfinished:

- old runtime names like `com.dgphoenix.*`
- old `MQ*` names and keys used in code/config/templates

We did **not** finish this earlier because it is risky. Many of these names are loaded at runtime from configuration files and database records. A blind rename could break the server.

This subproject exists to finish that cleanup safely.

## Why this was not completed in the main Phase 9 closeout
Phase 9 was completed as:
- rename tooling
- safety guards
- controlled review/apply workflow for low-risk waves

Phase 9 was **not** completed as:
- full runtime rename rollout

Reason:
- runtime-sensitive names were intentionally deferred to later approved waves (`W3+`)

## Objective
Safely remove or replace legacy runtime naming usage where possible, while preserving backward compatibility during migration.

## In Scope
- Inventory runtime-sensitive `com.dgphoenix.*` references
- Inventory runtime-sensitive `MQ*` / `mq` references
- Classify references by risk (safe text vs runtime-sensitive)
- Reuse Phase 9 tooling for governed execution
- Design and execute wrapper-based migration waves for runtime package/class names
- Design and execute controlled migration for `MQ*` keys/tokens where safe
- Produce evidence and validation after each wave

## Out of Scope (for this subproject)
- Blind global search/replace across the repo
- Cosmetic doc-only string cleanup that does not affect runtime behavior
- Renaming historical evidence artifacts for old phases
- Production cutover decisions (this subproject supports cleanup quality, not canary approval)

## Success Criteria
The subproject is complete when:
1. A reviewed inventory exists for runtime-sensitive legacy names.
2. Safe vs risky references are clearly separated.
3. Controlled migration waves are executed with evidence.
4. Runtime compatibility is preserved during migration (no regressions in launch/wallet/multiplayer validation).
5. Deferred high-risk items are explicitly documented if any remain.

## Risk Rules (mandatory)
- No blind replacement of `com.dgphoenix` or `mq` tokens.
- Any runtime class/package rename must use compatibility wrappers or equivalent migration safety.
- Any `MQ*` key rename must be verified against:
  - persisted bank/server configs
  - templates
  - runtime parsing/serialization behavior
  - support UI editing/validation behavior

## Dependencies (already available)
This subproject will reuse the existing Phase 9 tooling and manifests:
- compatibility map manifest
- review-only guards
- patch-plan export
- approval artifact workflow
- wave status reporting

## First Deliverables (this session)
- Runtime-sensitive inventory baseline
- Phase 9 tooling capability map (reused)
- Controlled execution plan for next waves
