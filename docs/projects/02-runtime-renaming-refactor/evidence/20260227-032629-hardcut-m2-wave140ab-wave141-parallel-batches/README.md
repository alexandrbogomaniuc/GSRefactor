# Evidence: 20260227-032629-hardcut-m2-wave140ab-wave141-parallel-batches

## Scope
- Planned parallel batches:
  - `W140A`: `common-promo/messages/{client/requests,server/notifications/prizes,server/responses}` declarations (`14`)
  - `W140B`: `sb-utils/src/test` declarations (`14`)
- Stabilized retained scope:
  - retained `W140A` declarations (`14`)
  - deferred `W140B` after compile incompatibility in `sb-utils` tests

## Validation
- `rerun1`:
  - fast gate FAIL at `step1` (`sb-utils` test compile due deferred-scope package drift)
- `rerun2`:
  - fast gate FAIL at `step3` (`common-gs` install ran tests; arm64 LZ4 native mismatch)
- `rerun3` (stabilized):
  - fast gate `5/5 PASS`
  - full matrix `9/9 PASS`
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `14`
- Deferred declaration migrations: `14` (`W140B`)
- Global metrics:
  - baseline `2277`
  - reduced `664`
  - remaining `1613`
  - burndown `29.161177%`
  - Project 02 `28.645147%`
  - Core `64.322574%`
  - Portfolio `82.161287%`
  - ETA `66.5h` (`8.31` workdays)
