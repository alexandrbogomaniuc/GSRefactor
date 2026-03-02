# 268 - Hard-cut live batchAV+AW (payment CommonException boundary normalization)

## Scope
Continuation stabilization wave after batchAT+AU, targeting the payment-lane exception boundary first-fail:
- `IPendingOperationProcessor` + implementing processors (`AddWinOperationProcessor`, `SitOutOperationProcessor`) `CommonException` namespace alignment.

## What was executed
- Batch AV (`2` rewires):
  - `IPendingOperationProcessor` import `CommonException` `com.dgphoenix -> com.abs`.
  - `AddWinOperationProcessor` import `CommonException` `com.dgphoenix -> com.abs`.
- Batch AW (`1` rewire):
  - `SitOutOperationProcessor` import `CommonException` `com.dgphoenix -> com.abs`.
- Total retained rewires: `3` import/signature-boundary bindings across `3` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL
  - first-fail moved to `pirates-math`:
    - `ShotCalculator` unresolved legacy `com.dgphoenix...RNG` import (`cannot find symbol RNG`).
- `mvn -pl games/clashofthegods -am -DskipTests compile`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` / DNS), not batch-local compile semantics.

## Measured movement
- Cleared payment-lane checked-exception mismatch lane (`AddWinOperationProcessor` unreported `CommonException`).
- `web` first-fail advanced to a narrower game-module import lane (`pirates-math` RNG namespace).
- `common-games` and `clashofthegods` consumer gates remain PASS.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-170619-hardcut-live-batchAVAW-payment-commonexception-rewire3/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `post-scan-signatures.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-first-fail.txt`
  - `fast-gate-status.txt`
  - `run-rerun1.sh`
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
- Remaining stabilization/import-normalization ETA: `~0.15-1.0h` (`~0.02-0.13` workdays), centered on:
  - `pirates-math` residual `RNG` namespace lane and immediate downstream web first-fail consumers.
