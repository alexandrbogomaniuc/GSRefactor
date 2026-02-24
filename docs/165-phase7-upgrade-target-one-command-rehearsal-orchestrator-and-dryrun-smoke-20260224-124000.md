# Phase 7 Upgrade Target One-Command Rehearsal Orchestrator And Dry-Run Smoke

Date: 2026-02-24

## What was added
- `gs-server/deploy/scripts/phase7-cassandra-upgrade-target-rehearsal.sh`
  - orchestrates target bootstrap/copy + source/target schema export + schema diff + rehearsal report generation.
  - supports `--dry-run true` for safe validation in restricted environments.
- `gs-server/deploy/scripts/phase7-cassandra-upgrade-target-rehearsal-smoke.sh`
  - validates dry-run execution and expected report markers.
- Shared verification suite now includes help + dry-run smoke for the new orchestrator.

## Why
- Phase 7 next execution step should be one command once Docker daemon image start/pull is available.
- Keeps current `c1` untouched and focuses testing on the separate `c1-refactor` target path.

## Validation
- shell syntax: pass
- dry-run smoke: `PHASE7_TARGET_REHEARSAL_SMOKE_OK`
- full local verification suite: pass (see latest report)

## Next live command
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-upgrade-target-rehearsal.sh
```
