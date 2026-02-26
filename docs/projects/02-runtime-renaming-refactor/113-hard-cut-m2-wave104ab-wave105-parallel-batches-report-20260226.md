# Hard-Cut M2 Wave 104A/104B + Wave 105 Report (Parallel Batches)

Date (UTC): 2026-02-26
Wave group: 104A + 104B + 105
Scope: execute batched-safe parallel hard-cut migration with non-overlapping ownership, then run integrated validation.

## Batch breakdown
- `W104A` (form/API batch): migrated 10 low-risk declaration packages to `com.abs`.
- `W104B` (support/cache/web batch): migrated 10 low-risk declaration packages to `com.abs` and applied bounded rewires in `log4j2.xml` and `support/getSessionError.jsp`.
- `W105` (integration validation): merged parallel edits and ran fast gate + full 9-step matrix.

## Changed files
- Full file list: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-202349-hardcut-m2-wave104ab-wave105-parallel-batches/target-files.txt`

## What changed
- Kept worker ownership non-overlapping (no shared Java/config ownership across workers).
- Applied only bounded rewires tied to moved `ParameterBuilder` and `SessionErrorsCache` classes.
- No blind global replace; runtime compatibility preserved.

## Validation evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-202349-hardcut-m2-wave104ab-wave105-parallel-batches/`
- Fast gate:
  - initial run failed due command-path issues (wrong reactor root and smoke script path), rerun with corrected commands passed (`web-gs package`, `refactor smoke`).
- Full matrix:
  - `validation-status.txt` (`9/9` PASS)

## Outcome metrics
- Scoped declaration migrations: `20 -> 0` legacy declarations, `20` `com.abs` declarations.
- Global tracked declarations/files remaining: `2006` (`2277` baseline, `271` reduced).
- Hard-cut burndown completion: `11.901625%`.
