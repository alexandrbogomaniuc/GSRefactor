# Hard-Cut M2 Wave 106A/106B + Wave 107 Report (Parallel Batches)

Date (UTC): 2026-02-26
Wave group: 106A + 106B + 107
Scope: execute batched-safe parallel hard-cut migration with non-overlapping ownership, then run integrated validation.

## Batch breakdown
- `W106A` (controller/config batch): migrated 11 low-risk declaration packages to `com.abs`.
- `W106B` (support/tool batch): migrated 19 low-risk declaration packages to `com.abs` and applied bounded rewires in Struts/JSP support files.
- `W107` (integration validation): merged parallel edits and ran fast gate + full 9-step matrix.

## Changed files
- Full file list: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-203744-hardcut-m2-wave106ab-wave107-parallel-batches/target-files.txt`

## What changed
- Kept worker ownership non-overlapping; no blind global replace.
- Applied only bounded rewires in `WEB-INF/struts-config.xml` and four support JSPs.
- Preserved runtime compatibility while increasing declaration burndown throughput.

## Validation evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-203744-hardcut-m2-wave106ab-wave107-parallel-batches/`
- Fast gate:
  - `fast-gate-status.txt` (`web-gs package`, `refactor smoke` PASS)
- Full matrix:
  - `validation-status.txt` (`9/9` PASS)

## Outcome metrics
- Scoped declaration migrations: `30 -> 0` legacy declarations, `30` `com.abs` declarations.
- Global tracked declarations/files remaining: `1976` (`2277` baseline, `301` reduced).
- Hard-cut burndown completion: `13.219148%`.
