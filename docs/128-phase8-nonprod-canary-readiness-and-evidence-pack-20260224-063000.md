# Phase 8 - Non-Prod Canary Readiness and Evidence Pack (2026-02-24)

## Scope
Execution-ready preparation for the final remaining Phase 8 blocker (`nonprod_canary_runtime`).
No GS restart or JVM flag mutation performed in this increment.

## What Changed
- Added Phase 8 non-prod canary readiness check:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-readiness-check.sh`
- Added Phase 8 non-prod canary evidence pack scaffold:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-evidence-pack.sh`
- Readiness/evidence scaffold captures:
  - runtime/offline readiness state (`READY`, `READY_OFFLINE_ONLY`, etc.)
  - current precision dual-calc log line count
  - generated matrix report path and blocking count
  - remaining blocker list
  - explicit JVM flag hint for Phase 8 precision canary run
- Integrated canary readiness/evidence tool help + logic smoke (offline-ok mode) into shared verification suite.
- Updated Phase 8 precision policy status:
  - `nonprod_canary_runtime` -> `execution_ready_pending_jvm_flags_and_run`
  - remains `blocking: true`
- Updated support docs/checklist/dashboard evidence to point `pu-precision-audit` to this doc (`doc 128`).

## Validation Performed
- Targeted readiness check (offline-ok in sandbox)
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-readiness-check.sh --allow-missing-runtime true`
  - output confirmed:
    - `status=READY_OFFLINE_ONLY`
    - `matrix_blocking_count=1`
    - JVM flag hint present
    - `notes=docker_api_unavailable` (sandbox limitation)
- Targeted canary evidence pack (offline-ok)
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-evidence-pack.sh --allow-missing-runtime true`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-nonprod-canary-evidence-20260224-050625.md`
- Regenerated matrix report (real output)
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-verification-matrix-20260224-050639.md`
  - summary remains:
    - `blockingCategories: 1`
    - `phase8ReadyToClose: no`
    - remaining blocker: `nonprod_canary_runtime` (`execution_ready_pending_jvm_flags_and_run`)
- Shared verification suite (with canary readiness/evidence scaffold checks included)
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260224-050623.md`
  - summary: `pass=50 fail=0 skip=0`
- Dashboard embedded sync
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - embedded checklist remains `26/41` (tooling/evidence increment only)
  - embedded fingerprint: `fp=7ae3b35e5d52`

## Phase 8 Closure Status (Current)
- Phase 8 is not complete yet.
- Generated blockers: `1`
- Remaining blocker: `nonprod_canary_runtime` (now execution-ready, but actual runtime canary evidence is still missing)

## Required Final Step To Close Phase 8
- Restart `refactor-gs-1` (non-prod only) with JVM flags:
  - `-Dabs.gs.phase8.precision.dualCalc.compare=true`
  - `-Dabs.gs.phase8.precision.scaleReady.apply=true`
  - `-Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3`
- Execute selected canary requests for a scale3 currency/bank profile.
- Re-run the canary evidence pack and capture:
  - `precision_dual_calc_log_lines > 0`
  - runtime evidence references
  - updated matrix/policy status before marking Phase 8 complete.

## Compatibility / Rollback
- Backward compatible; no runtime behavior change in this increment.
- Rollback: revert this commit; no schema/data impact.
