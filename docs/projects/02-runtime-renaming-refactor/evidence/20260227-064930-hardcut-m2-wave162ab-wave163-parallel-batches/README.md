# Hard-Cut M2 Wave 162A/162B + Wave 163 Evidence

Date (UTC): 2026-02-27
Wave group: 162A + 162B + 163

## Scope
- Batch A declarations: 10 (worker-owned)
- Batch B declarations: 10 (worker-owned)
- Bounded rewires: 3 (Batch B)
- Rewire overlap: 0

## Validation
- Fast gate:
  - Initial run + reruns 2-4 failed while resolving type-lineage compile breaks.
  - Targeted precheck `web-gs` rerun5 passed.
  - Final fast gate rerun6 passed `9/9` and was promoted to canonical:
    - `fast-gate-runner.log`
    - `fast-gate-status.txt`
    - `fast-gate-*.log`
- Full matrix:
  - rerun1 passed `9/9` with pre-setup installs and was promoted to canonical:
    - `validation-runner.log`
    - `validation-status.txt`
    - `01.log..09.log`
    - `pre-setup-*.log`

## Residual/Count Evidence
- Residual package checks:
  - `residual-batchA-namespaces.txt` (empty)
  - `residual-batchB-namespaces.txt` (empty)
- Global tracked declarations/files remaining:
  - `global-remaining-count.txt` -> `1324`
