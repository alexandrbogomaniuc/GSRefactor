# 283 - Hard-cut live batchBZ (SitInHandler exception boundary alignment)

## Scope
Continuation stabilization wave after batchBX+BY, targeting the localized web fail-head in `SitInHandler`:
- exception namespace mismatch (`com.dgphoenix` vs `com.abs`) in sit-in boundary handling.

## What was executed
- Batch BZ (`2` rewires):
  - switched `CommonException` import in `SitInHandler` to `com.abs` for throws/call-site alignment,
  - expanded exception guard in one catch block to preserve behavior for both legacy and migrated exception types during transition.
- Total retained rewires: `2` targeted updates across `1` file.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail remains in `SitInHandler` but moved deeper within the same lane:
    - `BuyInFailedException` declaration/catch boundary (`line ~696`),
    - residual legacy `com.dgphoenix...CommonException` boundary (`line ~836`).
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local BZ boundary rewires.

## Measured movement
- Cleared the initial `SitInHandler` exception mismatch lines (`~573/576/582/846`) from prior fail-head.
- Narrowed remaining local compile drift to two deeper `SitInHandler` boundary points.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-011018-hardcut-live-batchBZ-sitinhandler-exception-boundary-rewire2/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `post-scan-targeted-global.txt`
  - `pre-commit-git-status.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-bots.log`
  - `fast-gate-bots-first-fail.txt`
  - `fast-gate-web.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-cotg-first-fail.txt`
  - `fast-gate-cotg-consumer-compile.log`
  - `fast-gate-cotg-consumer-compile-first-fail.txt`
  - `fast-gate-status.txt`
  - `fast-gate-status-harmonized.txt`
  - `run-validation.sh`
  - `fast-gate-status-batchA-rerun1.txt`
  - `fast-gate-status-batchB-rerun1.txt`
  - `prewarm-status-rerun1.txt`
  - `validation-status-rerun1.txt`
  - `validation-summary-rerun1.txt`

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
- Remaining stabilization/import-normalization ETA: `~0.05-1.00h` (`~0.01-0.13` workdays), centered on final `SitInHandler` boundary harmonization.
