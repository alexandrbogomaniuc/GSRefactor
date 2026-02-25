# RENAME-FINAL Work Breakdown And Schedule

Last updated: 2026-02-25 UTC

## Phase 0: Refresh inventory baseline
1. Re-run runtime naming inventory scripts.
2. Split findings by risk level (`low`, `medium`, `high`).
3. Confirm exact migration target names and compatibility map.

Deliverables:
- refreshed inventory report,
- prioritized backlog,
- approved compatibility map.

## Phase 1: Low-risk wave (docs/config non-runtime-critical)
1. Rename safe text-only surfaces with guarded tooling.
2. Keep review-only rules for risky tokens.
3. Validate no behavioral impact.

Deliverables:
- low-risk wave patch set,
- verification report,
- rollback patch.

## Phase 2: Runtime class-string migration wave
1. Complete remaining reflection/class-loader compatibility points.
2. Move class config keys with dual-key support.
3. Validate startup and class loading for GS/MP.

Deliverables:
- runtime loader migration report,
- class-key alias matrix,
- startup proof logs.

## Phase 3: Runtime payload and template key migration wave
1. Migrate `MQ_*` payload/template key usage with dual-read/dual-write.
2. Verify GS-MP communication across legacy and new keys.
3. Update bank/template settings safely.

Deliverables:
- contract migration report,
- GS-MP compatibility evidence,
- template validation notes.

## Phase 4: Decommission preparation
1. Scan runtime for remaining legacy references.
2. Identify fallback paths eligible for removal.
3. Plan staged removal with rollback controls.

Deliverables:
- decommission candidate list,
- risk approvals,
- removal wave plan.

## Phase 5: Decommission and closure
1. Remove approved legacy compatibility code.
2. Re-run full integrated regression suite.
3. Publish final naming closure report.

Deliverables:
- decommission PRs,
- final verification report,
- closure document.

## Recommended schedule cadence
1. Week 1: Phase 0-1
2. Week 2: Phase 2
3. Week 3: Phase 3
4. Week 4: Phase 4-5

## Dependency and gating rules
- Phase 2 requires approved compatibility map.
- Phase 3 requires successful startup tests from Phase 2.
- Phase 5 cannot begin until no high-risk unresolved findings remain.
