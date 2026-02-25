# CASS-V4 Work Breakdown And Schedule

Last updated: 2026-02-25 UTC

## Current execution status
- Phase 0: in progress (Wave 1 baseline inventory evidence generated, dynamic tooling in place).
- Phase 1: in progress (initial dependency/API inventory completed; code migration map still pending).
- Phase 2+: not started.

## Phase 0: Freeze and baseline
1. Freeze dependency versions and lock baseline commit.
2. Export current driver inventory and dependency tree.
3. Capture current runtime health baseline and known warnings.

Deliverables:
- dependency baseline report,
- initial risk list,
- wave plan sign-off.

## Phase 1: Driver migration design
1. Decide approved Java driver 4.x version and support policy.
2. Map all APIs currently used from driver 3.x to 4.x replacements.
3. Define compatibility wrappers where direct migration is too risky.

Deliverables:
- API mapping sheet,
- module migration checklist,
- technical decision record.

## Phase 2: Code migration implementation
1. Migrate GS Cassandra access layer.
2. Migrate MP Cassandra access layer.
3. Update connection/session/pooling/timeouts for driver 4 semantics.
4. Add/adjust codecs/mappers where required.

Deliverables:
- merged migration PRs,
- compile/test pass reports,
- migration notes by module.

## Phase 3: Data migration and parity validation
1. Re-run schema export on source and target.
2. Re-run full copy (or incremental copy) into Cassandra 4 target.
3. Compare table counts for all required tables.
4. Verify critical financial and metadata tables explicitly.

Deliverables:
- schema diff report,
- parity matrix,
- unresolved mismatch list (if any).

## Phase 4: Runtime regression on Cassandra 4
1. Execute launch and gameplay smoke.
2. Execute wallet and financial lifecycle checks.
3. Execute multiplayer flow checks.
4. Execute reconnect/history checks.

Deliverables:
- runtime evidence pack,
- defect list with severity,
- fix/retest report.

## Phase 5: Performance and resilience
1. Compare baseline latency/error rates before vs after migration.
2. Run failure-injection tests (timeout, unavailable node, slow query).
3. Validate retry/idempotency behavior for financial paths.

Deliverables:
- performance comparison report,
- resilience test report,
- tuned config recommendations.

## Phase 6: Rollback rehearsal and sign-off
1. Execute rollback drill from v4 path to safe fallback.
2. Validate system recovers without data loss.
3. Final go/no-go review with complete evidence.

Deliverables:
- rollback rehearsal evidence,
- sign-off packet,
- closure summary.

## Recommended schedule cadence
1. Week 1: Phase 0-1
2. Week 2: Phase 2
3. Week 3: Phase 3-4
4. Week 4: Phase 5-6

## Dependency and gating rules
- Phase 2 cannot start before Phase 1 mapping is approved.
- Phase 4 cannot start before Phase 3 parity passes.
- Phase 6 cannot close until all high-severity defects are resolved.
