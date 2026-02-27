# Hard-Cut M2 Wave 164A/164B + Wave 165 Evidence

Date (UTC): 2026-02-27
Wave group: 164A + 164B + 165

## Scope Plan
- Batch A declarations planned: 12
- Batch B declarations planned: 12
- Planned rewires: 1 (Batch A), 0 (Batch B)
- Planned overlap: 0

## Stabilization Outcome
- Batch A retained as planned.
- Batch B was rolled back for safety after fast gate rerun1 surfaced broad `sb-utils` testCompile symbol-resolution failures from package-boundary migration.
- Retained scope:
  - declarations: 12 (Batch A only)
  - rewires: 1 (`ClusterConfig.xml`)

## Validation
- Fast gate:
  - rerun1 failed at `sb-utils` install after unsafe Batch B migration.
  - rerun2 passed `9/9` after Batch B rollback.
  - canonical artifacts promoted from rerun2.
- Full matrix:
  - rerun1 passed `9/9` (with pre-setup installs).
  - canonical artifacts promoted from rerun1.

## Residual/Count Evidence
- Residual package checks:
  - `residual-batchA-namespaces.txt` -> empty (all migrated)
  - `residual-batchB-namespaces.txt` -> non-empty by design (rollback retained `com.dgphoenix` namespaces)
- Rollback rationale:
  - `batchB-rollback-note.txt`
- Global tracked declarations/files remaining:
  - `global-remaining-count.txt` -> `1312`
