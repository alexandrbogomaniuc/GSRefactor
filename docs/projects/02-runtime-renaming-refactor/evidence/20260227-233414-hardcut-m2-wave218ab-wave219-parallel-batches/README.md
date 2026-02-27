# Evidence: Hard-Cut M2 Wave 218A + 218B + 219

## Scope
- W218A: 10 declaration migrations (`common/cache`, `common/cache/data/game`, `common/util`).
- W218B: 10 declaration migrations (`sb-utils/common/cache`, `sb-utils/common/util`).
- W219: bounded integration rewires + canonical validation.

## Planning Artifacts
- `target-batchA.txt`
- `target-batchB.txt`
- `rewires-batchA-all.txt`
- `rewires-batchB-all.txt`
- `overlap-metrics.txt`
- `package-checks.txt`
- `package-checks-after.txt`

## Stabilization Notes
- Fixed `STEP01` compile drift in `common` by aligning imports for moved declarations:
  - `IDistributedConfigCache`, `ICreateGameListener`, `MiniGameInfo`, `GameLanguageHelper`, `RoundFinishedHelper`.
- Fixed `STEP03` compile drift in `sb-utils` by aligning legacy-package interfaces to moved `com.abs` declarations:
  - `JsonSelfSerializable`, `JsonDeserializableDeserializer`, `JsonDeserializableModule` now import moved `JsonDeserializable`/`JsonAdditionalSerializer` explicitly.
- No blind/global replace used.

## Validation Runs
- Fast gate batchA:
  - rerun1: `STEP01 FAIL`
  - rerun2: `STEP01 FAIL`
  - rerun3: `STEP03 FAIL`
  - rerun4: non-canonical runner path drift (`STEP04` used wrong module path), discarded
  - rerun5 (canonical): `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- Full matrix:
  - rerun1: non-canonical PRE path drift (`PRE02` wrong module path), discarded
  - rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
  - retry1: `STEP09 FAIL (rc=2)`

## Canonical Result
- fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- retry1: `STEP09 FAIL (rc=2)`
