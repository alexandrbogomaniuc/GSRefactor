# Evidence: 20260227-040102-hardcut-m2-wave142ab-wave143-parallel-batches

## Scope
- Parallel batches:
  - `W142A`: `common/client/canex/request/{friends,onlineplayer}` declarations (`16`)
  - `W142B`: `common/client/canex/request/onlinerooms` + `common/transactiondata/storeddate/identifier` declarations (`10`)
- Integration:
  - `W143`: bounded integration verification only (no extra rewires required)

## Validation
- `rerun1`:
  - fast gate `5/5 PASS`
  - full matrix `9/9 PASS`
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `26`
- Deferred declaration migrations: `0`
- Global metrics:
  - baseline `2277`
  - reduced `690`
  - remaining `1587`
  - burndown `30.303030%`
  - Project 02 `28.787879%`
  - Core `64.393940%`
  - Portfolio `82.196970%`
  - ETA `65.4h` (`8.18` workdays)
