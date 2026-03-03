# 291 - Hard-cut live batchCLCM (STEP09 infra-classification hardening)

## Scope
Continuation stabilization wave after batchCJCK, targeting canonical `STEP09` smoke-lane signal quality and startup readiness ergonomics without touching runtime gameplay code paths.

## What was executed
- Batch CL+CM (`2` targeted hardening edits) across `2` files:
  - `gs-server/deploy/scripts/refactor-onboard.mjs`
    - Added dependency health probes (session-service, gameplay-orchestrator, wallet-adapter, protocol-adapter).
    - Added explicit infra-blocked classification path (`rc=3`) when launch alias fails and dependencies are down.
    - Preserved existing failure semantics (`rc=2`) when launch alias fails while dependencies are healthy.
  - `gs-server/deploy/scripts/refactor-start.sh`
    - Added bounded readiness wait helpers for service health endpoints in `up` flow.
    - Updated quick checks to stable endpoints (static asset JSON, config `/health`, GS support diagnostic route).
- No blind global replacement.
- No changes in mp declaration migration surfaces.

## Validation status
### Targeted fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: PASS
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`

### Step09 detail
- Retry log shows dependency probes all PASS and launch alias still FAIL (`HTTP 502`), so lane remains classified as functional smoke failure (`rc=2`), not infra-blocked:
  - `Dependency health:*` => `HTTP 200`
  - `Launch alias (startgame)` => `HTTP 502`

## Measured movement
- Improved diagnostic separation for `STEP09` by distinguishing dependency outages (`rc=3`) from healthy-dependency launch failures (`rc=2`).
- Added startup readiness gating in `refactor-start.sh` to reduce transient-start noise in manual/up workflows.
- Compile lanes stay fully green.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-054225-hardcut-live-batchCLCM-step09-infra-classification-hardening2/`
  - `pre-commit-git-status.txt`
  - `changed-files.txt`
  - `diff-integrated.patch`
  - `rewire-count.txt`
  - `post-scan-targeted-step09-hardening.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-bots.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-status.txt`
  - `run-validation.sh`
  - `fast-gate-status-batchA-rerun1.txt`
  - `fast-gate-status-batchB-rerun1.txt`
  - `prewarm-status-rerun1.txt`
  - `validation-status-rerun1.txt`
  - `validation-summary-rerun1.txt`
  - `STEP09-rerun1-retry1.log`

## Metrics
- Baseline declarations/files: `2277`
- Reduced: `2277`
- Remaining: `0`
- Hard-cut burndown: `100.000000%`
- Project 01: `100.000000%`
- Project 02: `54.645725%`
- Core total (01+02): `77.322863%`
- Entire portfolio: `88.661431%`

## ETA refresh
- Hard-cut declaration refactor ETA: `0.0h` (complete).
- Remaining stabilization/import-normalization ETA: `~0.01-0.15h` (`~0.00-0.02` workdays), dominated by persistent launch-alias `502` in `STEP09` with healthy dependency probes.
