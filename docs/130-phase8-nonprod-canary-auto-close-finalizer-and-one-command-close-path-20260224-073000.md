# Phase 8 Non-Prod Canary Auto-Close Finalizer and One-Command Close Path (20260224-073000 UTC)

## Summary
Added an automatic Phase 8 closure finalizer and wired it into the Phase 8 non-prod canary runner so a successful runtime canary can close Phase 8 in one command on a machine with Docker daemon write access.

## What Was Added
- `gs-server/deploy/scripts/phase8-precision-close-after-canary.sh`
  - validates non-prod canary runtime evidence (`status=READY`, `precision_dual_calc_log_lines >= 1`)
  - clears policy blocker `nonprod_canary_runtime`
  - regenerates precision verification matrix and requires `phase8ReadyToClose: yes`
  - marks checklist item `pu-precision-audit` as `done`
  - syncs precision policy copy + embedded dashboard snapshot
- `gs-server/deploy/scripts/phase8-precision-close-after-canary-smoke.sh`
  - synthetic runtime-ready evidence smoke for the finalizer
- `gs-server/deploy/scripts/phase8-precision-nonprod-canary-run.sh`
  - now auto-invokes the finalizer by default (`--auto-close-phase8 true`)

## One-Command Phase 8 Close Path (User Machine / Non-Prod)
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-run.sh
```

If runtime canary evidence is valid, the runner will now automatically:
1. generate runtime evidence pack,
2. close the last Phase 8 policy blocker,
3. regenerate matrix (`phase8ReadyToClose: yes`),
4. mark `pu-precision-audit` as done,
5. sync dashboard embedded progress JSON.

## Validation Performed (This Increment)
- Bash syntax checks for new/modified scripts
- Finalizer help output
- Synthetic finalizer smoke (`phase8-precision-close-after-canary-smoke.sh`) PASS
- Full local verification suite PASS

## Current Blocker (Sandbox Only)
This Codex sandbox still cannot perform Docker daemon write/recreate operations for the actual GS restart step (`docker compose up -d --force-recreate ...` via `/Users/alexb/.docker/run/docker.sock`).
The new one-command path removes the remaining manual policy/matrix/checklist closure work after you run the canary on your machine.
