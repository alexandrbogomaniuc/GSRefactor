# Evidence: 20260227-041057-hardcut-m2-wave144ab-wave145-parallel-batches

## Scope
- Parallel batches:
  - `W144A`: `common-gs/promo/tournaments/messages` declaration migrations (`13`)
  - `W144B`: `common-gs/promo/tournaments/messages` + `common-gs/battleground/messages` declaration migrations (`15`)
- Integration:
  - `W145`: bounded importer rewires and compatibility-stabilization rewires in `common-gs`

## Validation
- Fast gate:
  - `rerun1..5` failed during stabilization
  - `rerun6` passed (`5/5`)
- Full matrix:
  - `rerun1` passed (`9/9`)
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `28`
- Retained bounded rewires: `27`
- Deferred declaration migrations: `0`
- Global metrics:
  - baseline `2277`
  - reduced `718`
  - remaining `1559`
  - burndown `31.532718%`
  - Project 02 `28.941590%`
  - Core `64.470795%`
  - Portfolio `82.235397%`
  - ETA `64.2h` (`8.03` workdays)
