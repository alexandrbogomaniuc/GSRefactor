# Evidence: 20260227-052810-hardcut-m2-wave152ab-wave153-parallel-batches

## Scope
- Parallel batches:
  - `W152A`: `sb-utils/common/util/web` + `utils/common/util/web` declaration migrations (`20`)
  - `W152B`: `promo/persisters` declaration migrations (`18`)
- Integration:
  - `W153`: bounded importer rewires (`53` files), with 3 overlap files owned by main agent:
    - `GameServer.java`
    - `MQServiceHandler.java`
    - `TournamentManager.java`

## Validation
- Fast gate:
  - `rerun1` failed at `step8` (`common-gs`) due mixed canex request type lineage in `MQServiceHandler`.
  - bounded compatibility alignment applied in `MQServiceHandler` (imports + FQCN status types to `com.abs` lineage).
  - `rerun2` passed (`10/10`).
- Full matrix:
  - `rerun1` passed (`9/9`) with pre-setup installs: `utils`, `sb-utils`, `common-promo`, `promo-core`.
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `38`
- Retained bounded rewires: `53`
- Deferred declaration migrations: `0`
- Global metrics:
  - baseline `2277`
  - reduced `835`
  - remaining `1442`
  - burndown `36.671058%`
  - Project 02 `29.583882%`
  - Core `64.791941%`
  - Portfolio `82.395971%`
  - ETA `59.3h` (`7.42` workdays)
