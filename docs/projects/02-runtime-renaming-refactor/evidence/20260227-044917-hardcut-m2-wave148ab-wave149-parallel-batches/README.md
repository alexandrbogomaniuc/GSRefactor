# Evidence: 20260227-044917-hardcut-m2-wave148ab-wave149-parallel-batches

## Scope
- Parallel batches:
  - `W148A`: `sb-utils/common/util/string/mappers` + `sb-utils/common/util/xml/xstreampool` declaration migrations (`10`)
  - `W148B`: `sb-utils/common/vault` + `common/util/hardware/data` declaration migrations (`10`)
- Integration:
  - `W149`: bounded importer rewires (`6` files)

## Validation
- Fast gate:
  - `rerun1` failed due dependency-order issue (`common` not installed before `common-gs`)
  - `rerun2` passed (`6/6`)
- Full matrix:
  - `rerun1` failed at `step08` due mp-server command shape
  - `rerun2` passed (`9/9`)
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `20`
- Retained bounded rewires: `6`
- Deferred declaration migrations: `0`
- Global metrics:
  - baseline `2277`
  - reduced `776`
  - remaining `1501`
  - burndown `34.079930%`
  - Project 02 `29.259991%`
  - Core `64.629996%`
  - Portfolio `82.314998%`
  - ETA `61.8h` (`7.72` workdays)
