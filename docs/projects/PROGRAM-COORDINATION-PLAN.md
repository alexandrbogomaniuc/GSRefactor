# Program Coordination Plan

Last updated: 2026-02-25 UTC
Scope: final completion of core modernization goals in `Dev_new`.

## Program objective
Finish the two remaining high-impact tracks with production-grade quality:
- Cassandra 4 + Java driver 4 migration.
- Runtime class/package/config rename completion.

## Non-negotiable quality rules
1. Small waves only (no big-bang changes).
2. Backward compatibility first, cleanup second.
3. Every wave must have automated verification and human-readable evidence.
4. Rollback path must exist before rollout.
5. No scope creep until both tracks reach `SIGN_OFF_READY`.

## Track structure
1. Track A: `01-cassandra-v4-driver-migration`
2. Track B: `02-runtime-renaming-refactor`

Each track has its own:
- charter,
- work breakdown,
- test matrix,
- documentation checklist,
- risk and rollback plan,
- sign-off gates.

## Execution order (recommended)
1. Complete Track A Wave A-C first (driver + compatibility + rehearsal baseline).
2. Run Track B in parallel only for inventory and compatibility prep.
3. Freeze Track B large rename applies while Track A does cutover rehearsal.
4. After Track A `GO_NO_GO_REHEARSAL` passes, execute Track B migration waves.
5. Run final integrated full-system validation across both tracks.

Reason: this sequencing reduces the chance that two risky runtime changes overlap.

## Integrated timeline model
1. Program Week 1
- Lock baseline, branch strategy, and evidence templates.
- Re-run inventories and preflight checks.

2. Program Week 2
- Track A: driver/code migration implementation.
- Track B: runtime-sensitive inventory refresh and compatibility mapping updates.

3. Program Week 3
- Track A: full data parity and mixed-topology rehearsal.
- Track B: controlled rename wave apply (low-risk first).

4. Program Week 4
- Track B: medium/high-risk rename waves with compatibility toggles.
- Full integrated regression, performance, and rollback drills.

5. Program Week 5
- Closure: remove only proven-safe legacy paths, publish final report, sign-off.

## Branch and merge policy
- Suggested long-lived branches:
  - `project/cassandra-v4-driver`
  - `project/runtime-rename-final`
- Merge model:
  - PRs into each project branch,
  - periodic integration PR into `main` only after matrix pass.
- Release tags:
  - `trackA-wave-*`, `trackB-wave-*`, `program-closure-*`.

## Shared test cadence
1. Per-commit checks
- module compile/tests for touched modules,
- lint/static checks,
- targeted smoke tests.

2. Per-wave checks
- `phase5-6-local-verification-suite.sh`,
- track-specific matrices,
- runtime launch validation,
- evidence pack generation.

3. Pre-signoff checks
- full matrix pass for both tracks,
- rollback drill success,
- checklist signed with links to evidence.

## Shared evidence contract
For each wave, store:
- `summary-<timestamp>.md`
- `command-log-<timestamp>.txt`
- `verification-report-<timestamp>.md`
- `rollback-drill-<timestamp>.md` (if applicable)

Track evidence paths:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/01-cassandra-v4-driver-migration/evidence`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence`

## Program completion definition
Program is complete only when all are true:
1. Track A and Track B reach `SIGN_OFF_READY`.
2. End-to-end launch/wallet/multiplayer checks pass on final runtime.
3. No critical or high unresolved findings remain.
4. Finalization report is updated with exact evidence links.
