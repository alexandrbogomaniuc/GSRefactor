# Phase 8 - Non-Prod Canary Execution Script and Sandbox Blocker (2026-02-24)

## Scope
Final blocker preparation for Phase 8 runtime canary execution.
Adds an executable canary runner and documents the environment limitation that prevents completing the final canary in this sandbox.

## What Changed
- Added `GS_JAVA_OPTS` passthrough for refactor GS container startup (default empty / no behavior change):
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/gs/Dockerfile`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml` (`gs` service env)
- Added executable non-prod canary runner script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-run.sh`
  - supports `--dry-run`, rebuild/recreate, startgame trigger, evidence-pack invocation, and optional restore-to-default restart
- Integrated canary runner help + dry-run path into shared verification suite (`phase5-6-local-verification-suite.sh`).
- Updated Phase 8 policy status string for the last blocker:
  - `nonprod_canary_runtime` -> `execution_script_ready_pending_jvm_flags_and_run` (still blocking)
- Updated support docs/checklist/dashboard evidence to point `pu-precision-audit` to this doc (`doc 129`).

## Runtime Execution Attempt (Blocked Here)
Attempted direct non-prod GS canary execution in this environment:
- `docker compose ... up -d --build --force-recreate --no-deps gs` with Phase 8 JVM flags
- Result: blocked by sandbox Docker daemon write permission
- Error (captured): permission denied while trying to connect to Docker daemon socket (`/Users/alexb/.docker/run/docker.sock`) for image/recreate operations

## Validation Performed
- Canary runner local checks (offline)
  - `bash -n phase8-precision-nonprod-canary-run.sh` ✅
  - `phase8-precision-nonprod-canary-run.sh --help` ✅
  - `phase8-precision-nonprod-canary-run.sh --dry-run true --build-gs false --wait-seconds 1` ✅
  - dry-run prints full executable command sequence (restart with flags -> inspect args -> startgame trigger -> evidence pack -> restore default)
- Policy sync + dashboard sync
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-phase8-precision-policy.sh` ✅
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` ✅
  - dashboard embedded fingerprint: `fp=53a05d150a2b`
- Shared verification suite (with canary runner help/dry-run path included)
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260224-065200.md`
  - summary: `pass=50 fail=0 skip=0`
- Regenerated matrix report (real output)
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-verification-matrix-20260224-065214.md`
  - summary remains:
    - `blockingCategories: 1`
    - `phase8ReadyToClose: no`
    - `nonprod_canary_runtime` = `execution_script_ready_pending_jvm_flags_and_run`

## Exact Command To Finish Phase 8 (Run On Your Machine)
From `/Users/alexb/Documents/Dev/Dev_new`:

```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-run.sh
```

Then:
1. Review generated canary evidence pack + matrix reports under `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/`
2. If `precision_dual_calc_log_lines > 0` and canary request path is successful, update policy `nonprod_canary_runtime` to non-blocking and regenerate matrix
3. Mark `pu-precision-audit` done only after matrix shows `phase8ReadyToClose: yes`

## Compatibility / Rollback
- Backward compatible by default (`GS_JAVA_OPTS` empty).
- Rollback: revert this commit; no schema/data impact.
