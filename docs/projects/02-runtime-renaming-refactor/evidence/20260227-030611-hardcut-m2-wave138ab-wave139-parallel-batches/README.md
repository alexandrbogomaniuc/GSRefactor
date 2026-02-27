# Evidence: 20260227-030611-hardcut-m2-wave138ab-wave139-parallel-batches

## Scope
- Planned parallel batches:
  - `W138A`: `common-gs/kafka/dto/privateroom/{request,response}` declarations (+ bounded common-gs rewires)
  - `W138B`: `sb-utils/common/vault` + `sb-utils/common/util/xml/xstreampool` declarations (+ bounded rewire)
- Stabilized retained scope:
  - retained `W138A`
  - deferred `W138B` for compatibility safety

## Validation
- `rerun1`: FAIL (`step3/step5`) due `common-gs` unresolved `xstreampool` package after B-slice rename.
- `rerun2` (stabilized A-only):
  - fast gate `5/5 PASS`
  - full matrix `9/9 PASS`
- Canonical promoted artifacts:
  - `fast-gate-status.txt`
  - `validation-status.txt`
  - `validation-runner.log`
  - `01.log..09.log`

## Outcome
- Retained declaration migrations: `15`
- Retained rewires: `3`
- Global metrics:
  - baseline `2277`
  - reduced `650`
  - remaining `1627`
  - burndown `28.546333%`
  - Project 02 `28.568292%`
  - Core `64.284146%`
  - Portfolio `82.142073%`
  - ETA `67.1h` (`8.39` workdays)
