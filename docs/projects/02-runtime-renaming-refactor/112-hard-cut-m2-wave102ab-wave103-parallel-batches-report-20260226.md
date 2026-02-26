# Hard-Cut M2 Wave 102A/102B + Wave 103 Report (Parallel Batches)

Date (UTC): 2026-02-26
Wave group: 102A + 102B + 103
Scope: execute batched-safe parallel hard-cut migration with non-overlapping ownership, then run integrated validation.

## Batch breakdown
- `W102A` (controller response batch): migrated 13 FRB transport + MQB response declarations to `com.abs`, with bounded dependent rewires in `BattlegroundPrivateRoomController`.
- `W102B` (entity/lobby/web batch): migrated 12 low-fanout declarations to `com.abs` plus bounded dependent rewires in lobby/start-game/login/game-history/cache paths.
- `W103` (integration validation): merged parallel edits, applied compatibility fix for game-history list typing, and reran gates/matrix.

## Changed files
- Full file list: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-200827-hardcut-m2-wave102ab-wave103-parallel-batches/target-files.txt`

## What changed
- Kept worker ownership non-overlapping; no blind global replace.
- Applied only bounded import rewires required by moved classes.
- Resolved compatibility regression by restoring `GameHistoryListEntry` to legacy package and aligning dependent imports.
- Preserved runtime compatibility while retaining net declaration burndown.

## Validation evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-200827-hardcut-m2-wave102ab-wave103-parallel-batches/`
- Fast gate:
  - `fast-gate-status.txt` (initial `web-gs package` fail, rerun PASS; `refactor smoke` PASS)
- Full matrix:
  - `validation-status.txt` (`9/9` PASS)

## Outcome metrics
- Scoped declaration migrations (37 changed files in integration scope):
  - pre legacy declarations: `37`
  - post legacy declarations: `13`
  - post `com.abs` declarations: `24`
- Global tracked declarations/files remaining: `2026` (`2277` baseline, `251` reduced).
- Hard-cut burndown completion: `11.023276%`.
