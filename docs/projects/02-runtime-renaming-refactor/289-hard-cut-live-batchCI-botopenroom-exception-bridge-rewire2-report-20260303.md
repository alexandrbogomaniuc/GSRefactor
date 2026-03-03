# 289 - Hard-cut live batchCI (BotOpenRoomHandler exception bridge)

## Scope
Continuation stabilization wave after batchCH, targeting localized web fail-head in `BotOpenRoomHandler`.

## What was executed
- Batch CI (`1` targeted rewire):
  - in `BotOpenRoomHandler#openRoom(...)`, added ABS-to-legacy bridge around `room.processOpenRoom(...)`:
    - catches `com.abs...CommonException`,
    - rethrows legacy `com.dgphoenix...CommonException` with preserved message/cause.
- Total retained rewires: `1` across `1` file.
- No behavior-flow changes and no global replacement.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: PASS
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local CI rewires.

## Measured movement
- Cleared localized `BotOpenRoomHandler` compile fail-head (`[88,55]` lane).
- Web compile gate is now green in targeted fast-gate run.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-040534-hardcut-live-batchCI-botopenroom-exception-bridge-rewire2/`
  - `pre-commit-git-status.txt`
  - `changed-files.txt`
  - `diff-integrated.patch`
  - `rewire-count.txt`
  - `post-scan-targeted-botopenroom-boundaries.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-bots.log`
  - `fast-gate-bots-first-fail.txt`
  - `fast-gate-web.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-cotg-first-fail.txt`
  - `fast-gate-status.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.01-0.25h` (`~0.00-0.03` workdays), dominated by known external STEP09/cotg lane.
