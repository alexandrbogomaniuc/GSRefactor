# Hard-Cut M2 Wave 98A/98B + Wave 99 Report (Parallel Batches)

Date (UTC): 2026-02-26
Wave group: 98A + 98B + 99
Scope: execute batched-safe parallel hard-cut migration with non-overlapping ownership and integrated validation.

## Batch breakdown
- `W98A` (support/diagnosis batch): migrated 17 class package declarations from `com.dgphoenix` to `com.abs` in:
  - `support/tool/*`
  - `web/system/diagnosis/*`
  - `web/system/diagnosis/tasks/*`
- `W98B` (bonus/frbonus form batch): migrated 10 form package declarations from `com.dgphoenix` to `com.abs`, updated dependent action imports, and rewired corresponding Struts form-bean types.
- `W99` (integration): updated `WEB-INF/web.xml` servlet-class FQCNs for diagnosis servlets after package migration.

## Changed files
- Full file list: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-193037-hardcut-m2-wave98ab-wave99-parallel-batches/target-files.txt`

## What changed
- Preserved runtime compatibility by avoiding global replace and limiting edits to owned, low-risk batches.
- Kept Struts ownership isolated to the bonus/frbonus worker batch.
- Added integration rewire for `web.xml` to avoid servlet loading regressions after diagnosis package migration.

## Validation evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-193037-hardcut-m2-wave98ab-wave99-parallel-batches/`
- Fast gate:
  - `fast-gate-status.txt` (PASS on rerun)
- Full matrix:
  - `validation-status.txt` (9/9 PASS)

## Outcome metrics
- Scoped declarations (27 moved classes): `27 -> 0` legacy declarations, `27` `com.abs` declarations.
- Global tracked declarations/files remaining: `2077` (`2277` baseline, `200` reduced).
- Hard-cut burndown completion: `8.783487%`.
