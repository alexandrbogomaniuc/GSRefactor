# Hard-Cut M2 Wave 166A/166B + Wave 167 Evidence

Date (UTC): 2026-02-27
Wave group: 166A + 166B + 167

## Scope Plan
- Batch A declarations planned: 10
- Batch B declarations planned: 10
- Planned rewires:
  - Batch A: 5
  - Batch B: 20
  - overlap: 3

## Stabilized Retained Scope
- Batch A retained: 10 declarations.
- Batch B retained: 1 declaration (`PersistersFactory`) due shared dependency path.
- Additional retained bounded rewire: `IKeyspaceManager`.
- Retained total declarations/files: 12.

## Stabilization
- Fast gate rerun1 failed at `common-persisters` install because Batch B + broad rewires crossed not-yet-migrated dependency boundaries.
- Rolled back main-owned Batch B and overlap rewires.
- Kept only safe retained subset above.
- See `batchB-rollback-note.txt` for details.

## Validation
- Fast gate:
  - rerun1 FAIL (captured)
  - rerun2 PASS `9/9` and promoted to canonical
- Full matrix:
  - rerun1 PASS `9/9` and promoted to canonical

## Residual/Count Evidence
- `residual-batchA-namespaces.txt` -> empty
- `residual-batchB-namespaces.txt` -> non-empty by design (rollback)
- `global-remaining-count.txt` -> `1300`
