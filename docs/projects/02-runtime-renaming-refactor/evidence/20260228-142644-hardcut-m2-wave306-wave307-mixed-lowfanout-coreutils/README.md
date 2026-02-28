# Evidence Summary - Hard-Cut M2 Wave 306 + 307

Timestamp (UTC): 2026-02-28 14:26-14:57
Wave group: 306 + 307
Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260228-142644-hardcut-m2-wave306-wave307-mixed-lowfanout-coreutils/`

## Retained Declarations
- `CurrencyRate`
- `ICurrencyRateManager`
- `BonusException`
- `BonusError`
- `CommonWalletErrors`
- `ReflectionUtils`
- `DigitFormatter`
- `KryoHelper`
- `JsonSelfSerializable`
- `CacheKeyInfo`

## Deferred Declarations
- none

## Validation Progression
- `rerun1-rerun5`: failed in `STEP01/STEP05` while resolving compile-order and mixed exception/type boundaries.
- `rerun6-rerun10`: `STEP01-05` stabilized; iterative `STEP06` boundary fixes applied.
- `rerun11`: canonical profile reached.

## Canonical Validation Outcome (rerun11)
- fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL rc=2`
- fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL rc=2`
- full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL rc=2`, retry1 `rc=2`
