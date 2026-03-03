# 287 - Hard-cut live batchCG (NicknameService exception bridge)

## Scope
Continuation stabilization wave after batchCF, targeting the localized web fail-head in `NicknameService` exception boundaries.

## What was executed
- Batch CG (`3` targeted rewires):
  - added local ABS-to-legacy exception bridges in `NicknameService` methods that kept legacy contract:
    - `addReservedNicknameForEntireSystem(...)`
    - `removeReservedNicknameForEntireSystem(...)`
    - `changeNickname(...)`
  - each path now catches `com.abs.casino.common.exception.CommonException` and rethrows legacy `com.dgphoenix...CommonException` with preserved message/cause.
- Total retained rewires: `3` across `1` file.
- No business-flow changes and no global replacement.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off `NicknameService` into localized `GetBattlegroundStartGameUrlHandler`:
    - `GetBattlegroundStartGameUrlHandler.java:[293,44]` unreported legacy `com.dgphoenix...CommonException`
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local CG rewires.

## Measured movement
- Cleared `NicknameService` compile fail-head (`[47,45]`, `[54,48]`, `[84,54]`).
- Advanced web fail frontier to localized `GetBattlegroundStartGameUrlHandler` boundary point (`[293,44]`).

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-031511-hardcut-live-batchCG-nickname-exception-bridge-rewire3/`
  - `pre-commit-git-status.txt`
  - `changed-files.txt`
  - `diff-integrated.patch`
  - `rewire-count.txt`
  - `post-scan-targeted-nickname-boundaries.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.01-0.35h` (`~0.00-0.04` workdays), centered on localized `GetBattlegroundStartGameUrlHandler` exception-boundary harmonization.
