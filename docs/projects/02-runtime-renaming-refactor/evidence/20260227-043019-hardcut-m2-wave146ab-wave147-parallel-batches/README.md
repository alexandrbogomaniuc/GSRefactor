# Evidence: 20260227-043019-hardcut-m2-wave146ab-wave147-parallel-batches

## Scope
- Parallel batches:
  - `W146A`: `sb-utils/common/mp` declaration migrations (`20`)
  - `W146B`: `sb-utils/common/util/xml` + `common-gs` xml test declaration migrations (`18`)
- Integration:
  - `W147`: bounded import rewires across dependent modules (`36` files), including one compatibility bridge fix in `common-wallet`.

## Validation
- Fast gate:
  - `rerun1..2` failed during stabilization (dependency order + mixed-type drift)
  - `rerun3` passed (`5/5`)
- Full matrix:
  - `rerun1` failed at step02 (`common-wallet` compile)
  - `rerun2` passed (`9/9`)
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `38`
- Retained bounded rewires: `36`
- Deferred declaration migrations: `0`
- Global metrics:
  - baseline `2277`
  - reduced `756`
  - remaining `1521`
  - burndown `33.201581%`
  - Project 02 `29.150198%`
  - Core `64.575099%`
  - Portfolio `82.287549%`
  - ETA `62.6h` (`7.83` workdays)
