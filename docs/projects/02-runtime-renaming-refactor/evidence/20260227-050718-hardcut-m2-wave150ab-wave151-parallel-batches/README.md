# Evidence: 20260227-050718-hardcut-m2-wave150ab-wave151-parallel-batches

## Scope
- Parallel batches:
  - `W150A`: `sb-utils/common/util/support` + `utils/common/util/system` declaration migrations (`11`)
  - `W150B`: `common/client/canex/request/privateroom` + `common-promo/messages/server/notifications/tournament` declaration migrations (`10`)
- Integration:
  - `W151`: bounded importer rewires (`26` files)

## Validation
- Fast gate:
  - `rerun1` fail at step1 (`IJsonCWClient` stale import)
  - `rerun2` fail at step1 (dependency-order drift)
  - `rerun3` fail at step4 (`CanexCWClient` type-identity mismatch)
  - `rerun4` pass (`9/9`)
- Full matrix:
  - `rerun1` pass (`9/9`) with pre-setup installs: `utils`, `sb-utils`, `common-promo`
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `21`
- Retained bounded rewires: `26`
- Deferred declaration migrations: `0`
- Global metrics:
  - baseline `2277`
  - reduced `797`
  - remaining `1480`
  - burndown `35.002196%`
  - Project 02 `29.375274%`
  - Core `64.687637%`
  - Portfolio `82.343819%`
  - ETA `60.9h` (`7.62` workdays)
