# Evidence: Hard-Cut M2 Wave 224A + 224B + 225

## Scope
- W224A: 10 declaration migrations (`game-server/common-gs` handler/dto/exception/task surfaces).
- W224B: 10 declaration migrations (`game-server/common-gs/kafka/dto`) + 10 bounded rewires (`mp-server/web` kafka handlers).
- W225: bounded integration stabilization + canonical validation.

## Planning Artifacts
- `target-batchA.txt`
- `target-batchB.txt`
- `rewires-batchA-all.txt`
- `rewires-batchB-all.txt`
- `overlap-metrics.txt`
- `rationale.txt`
- `package-checks.txt`
- `package-checks-after.txt`

## Stabilization Notes
- BatchA rerun1 and rerun2 failed at `STEP06` due moved declarations losing same-package visibility to legacy types.
- Applied minimal import-only stabilization in moved declarations:
  - `KafkaOuterRequestHandler` / `KafkaInServiceRequestHandler` imports in moved handlers.
  - `AbstractSendAlertException` import in moved `GameSessionNotFoundException`.
  - `KafkaRequest` and `BGPlayerDto` imports in moved `UpdatePrivateRoomRequest`.
- BatchB worker also added minimal compatibility imports in moved dto declarations:
  - `KafkaRequest` in moved request declarations.
  - `BotConfigInfoDto` in moved `UpsertBotConfigInfoRequest`.
- No blind/global replacement performed.

## Validation Runs
- Fast gate batchA:
  - rerun1: `STEP06 FAIL`
  - rerun2: `STEP06 FAIL`
  - rerun3 (canonical): `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
  - retry1: `STEP09 FAIL (rc=2)`

## Canonical Result
- fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- retry1: `STEP09 FAIL (rc=2)`
