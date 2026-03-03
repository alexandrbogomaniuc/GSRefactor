# Hard-cut Live Batch CN+CO - STEP09 Upstream Resilience

- Timestamp (UTC): 2026-03-03 06:03-06:15
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Scope: Runtime-safe stabilization for persistent `STEP09` launch alias `HTTP 502` failures.
- Batch size: `17` targeted declarations/edits (config/orchestration + diagnostics).

## Summary
Batch `CN+CO` hardened both startup orchestration and smoke diagnostics without touching gameplay/runtime business logic.

- `CN` (orchestration/config): moved GS upstream host mapping to stable compose service alias (`gs`), tightened nginx resolver behavior, added restart policies for `gs`/`static`, and added readiness + warm alias probe in `refactor-start.sh`.
- `CO` (diagnostics/classification): added direct GS launch probe in smoke checks, separated GS-direct/support probe signals from dependency probes, emitted nginx upstream hints, and promoted alias failures with upstream-down signals to `INFRA-BLOCKED` (`rc=3`).

## Files Changed
- `gs-server/deploy/config/cluster-hosts.properties`
- `gs-server/deploy/docker/configs/.env` (generated)
- `gs-server/deploy/docker/configs/static/cluster-hosts.inc` (generated)
- `gs-server/deploy/docker/configs/static/games`
- `gs-server/deploy/docker/refactor/.cluster-hosts.env` (generated)
- `gs-server/deploy/docker/refactor/.env` (generated)
- `gs-server/deploy/docker/refactor/README.md`
- `gs-server/deploy/docker/refactor/docker-compose.yml`
- `gs-server/deploy/scripts/refactor-onboard.mjs`
- `gs-server/deploy/scripts/refactor-start.sh`
- `gs-server/game-server/web-gs/src/main/resources/cluster-hosts.properties` (synced)

## Validation
Evidence directory:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-060341-hardcut-live-batchCNCO-step09-upstream-resilience/`

Fast gate compile results:
- `common_games_compile=0`
- `bots_compile=0`
- `web_compile=0`
- `cotg_compile=0`

Full matrix summary:
- `fast_gate_batchA FAIL STEP09`
- `fast_gate_batchB FAIL STEP09`
- `prewarm PASS`
- `validation FAIL STEP09`
- `step09_retry1 FAIL rc=3`

Key behavioral improvement:
- `STEP09` retry now classifies as `INFRA-BLOCKED` (`rc=3`) when alias fails with GS direct/support probes down.
- Retry log includes explicit nginx upstream hints (`connect() failed`, resolver/upstream context), reducing manual triage overhead.

## Metrics
- Baseline tracked declarations/files: `2277`
- Reduced: `2277`
- Remaining: `0`
- Burndown completion: `100.000000%`

Weighted metrics:
- Project 01: `100.000000%`
- Project 02: `54.645725%`
- Core total (01+02): `77.322863%`
- Entire portfolio: `88.661431%`

## ETA Refresh
- Refactor declaration migration: complete (`0` remaining).
- Residual runtime closure (STEP09 upstream stability): `~0.25-4.00h` (`~0.03-0.50` workdays), dependent on GS/static upstream health under sustained smoke retries.
