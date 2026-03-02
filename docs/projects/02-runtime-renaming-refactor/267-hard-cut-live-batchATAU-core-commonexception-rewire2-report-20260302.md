# 267 - Hard-cut live batchAT+AU (core CommonException inheritor cleanup)

## Scope
Continuation stabilization wave after batchAR+AS, targeting the narrowed `web` first-fail lane in `mp-server/core`:
- `BGPrivateRoomInfoService` mixed `CommonException` namespace catch boundary.
- low-risk adjacent `IdGenerator` import cleanup.

## What was executed
- Batch AT (`1` rewire):
  - `BGPrivateRoomInfoService` import `CommonException` `com.dgphoenix -> com.abs`.
- Batch AU (`1` rewire):
  - `IdGenerator` import `CommonException` `com.dgphoenix -> com.abs`.
- Total retained rewires: `2` import/signature-boundary bindings across `2` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL
  - first-fail moved to `AddWinOperationProcessor`:
    - unreported `com.abs...CommonException` at call boundary (`must be caught or declared to be thrown`).
- `mvn -pl games/clashofthegods -am -DskipTests compile`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` / DNS), not batch-local compile semantics.

## Measured movement
- Cleared prior `BGPrivateRoomInfoService` mixed exception namespace first-fail.
- `web` first-fail moved forward to the next boundary consumer (`AddWinOperationProcessor`) while `common-games` and `clashofthegods` remained PASS.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-165532-hardcut-live-batchATAU-core-commonexception-rewire2/`
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
- Remaining stabilization/import-normalization ETA: `~0.3-1.5h` (`~0.04-0.19` workdays), centered on:
  - `AddWinOperationProcessor` exception boundary handling and dependent payment-lane consumers.
