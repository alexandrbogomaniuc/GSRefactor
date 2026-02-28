# Evidence: 20260228-170717-hardcut-m2-wave314-wave315-kafka-dto-battleground-core

## Scope
- Batch A declarations: `target-batchA.txt` (`8` files)
- Batch B declarations: `target-batchB.txt` (`8` files)
- Retained declarations: `target-retained.txt` (`16` files)
- Deferred declarations: `target-deferred.txt` (`# none`)

## Validation
- `rerun1`: fails at `STEP06` (compile boundary drift after package moves).
- `rerun2`: fails at `STEP06` (wildcard import resolution drift).
- `rerun3` canonical:
  - fast gate batchA/batchB: `STEP01-08 PASS`, `STEP09 rc=2`
  - full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 rc=2` (retry1 `rc=2`).

## Key artifacts
- `rewires-fqcn-before.txt`
- `rewires-fqcn-after.txt`
- `validation-summary-rerun3.txt`
- `fast-gate-status-batchA-rerun3.txt`
- `fast-gate-status-batchB-rerun3.txt`
- `prewarm-status-rerun3.txt`
- `validation-status-rerun3.txt`
- `run-rerun1.sh`, `run-rerun2.sh`, `run-rerun3.sh`
