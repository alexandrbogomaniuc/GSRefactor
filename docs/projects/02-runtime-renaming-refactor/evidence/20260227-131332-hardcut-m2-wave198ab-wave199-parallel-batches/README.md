# Evidence Summary - W198A/W198B + W199

## Scope
- `W198A`: declaration migrations in `services*` and `transactiondata*`.
- `W198B` safe subset: `UnsupportedCurrencyException`, `RESTServiceClient`.
- `W199`: integration and validation.

## Final Validation Snapshot
- Fast gate canonical: `fast-gate-status-rerun8.txt`
  - `PRE01-03 PASS`
  - `STEP01-08 PASS`
  - `STEP09 FAIL` (`HTTP 502` launch alias)
- Full matrix canonical: `validation-status-rerun1.txt`
  - `PRE01-03 PASS`
  - `STEP01-08 PASS`
  - `STEP09 FAIL` (`rc=2`; recovery retry executed once)

## Key Artifacts
- Targets:
  - `target-batchA.txt`
  - `target-batchB.txt`
- Rewires:
  - `rewires-batchA-all.txt`
  - `rewires-batchB-all.txt`
- Fast gate:
  - `fast-gate-runner-rerun8.log`
  - `fast-gate-STEP01-rerun8.log` ... `fast-gate-STEP09-rerun8.log`
  - `fast-gate-status-rerun8.txt`
- Full matrix:
  - `validation-runner-rerun1.log`
  - `STEP01-rerun1.log` ... `STEP09-rerun1.log`
  - `validation-status-rerun1.txt`
