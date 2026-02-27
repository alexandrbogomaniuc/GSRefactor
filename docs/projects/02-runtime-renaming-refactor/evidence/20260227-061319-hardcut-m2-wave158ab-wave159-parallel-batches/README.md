# Evidence: 20260227-061319-hardcut-m2-wave158ab-wave159-parallel-batches

## Scope
- Parallel batches:
  - `W158A`: `gs.maintenance` + `gs.maintenance.converters` + `gs.managers.payment.wallet.common.xml` declaration migrations (`12`)
  - `W158B`: `common.promo.ai` + `gs.managers.payment.bonus.client.frb` declaration migrations (`10`)
- Integration:
  - `W159`: bounded importer rewires (`9` files)

## Validation
- Fast gate:
  - initial run failed at `common-persisters` due dependency order (missing `common-promo` install after `common.promo.ai` migration).
  - rerun2 pass (`9/9`) after adding `common-promo` install step.
- Full matrix:
  - `rerun1` pass (`9/9`) with pre-setup installs: `utils`, `sb-utils`, `common-promo`.
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `22`
- Retained bounded rewires: `9`
- Deferred declaration migrations: `0`
- Global metrics:
  - baseline `2277`
  - reduced `913`
  - remaining `1364`
  - burndown `40.096618%`
  - Project 02 `30.012077%`
  - Core `65.006039%`
  - Portfolio `82.503019%`
  - ETA `56.2h` (`7.02` workdays)
