# 261 - Hard-cut live batchAF+AG (common-games import rewire)

## Scope
Continuation stabilization wave focused on the final residual legacy imports in `mp-server/games/common-games` after batchAD+AE.

## What was executed
- Batch AF: `11` import rewires across `11` files (`CommonException` lanes in events/waiting/socket flows).
- Batch AG: `11` import rewires across `8` files (`Pair` + testmodel utility interfaces/exceptions).
- Total retained rewires: `22` declarations across `19` files.
- Import-only rewires from `com.dgphoenix` to `com.abs` for:
  - `CommonException`, `Pair`, `CBGameException`, `LongIdGenerator`, `ISequencer`, `ISequencerPersister`.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- `mvn -pl web -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- `mvn -pl games/clashofthegods -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- First-fail signature moved off residual legacy-import symbols to:
  - `StubCurrency` single-type import collision (`ICurrency`)
  - broader `mp-common-games` API/type boundary mismatches (`IGameState`/`IPlayerRoundInfo` method contract incompatibilities).

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`

## Measured movement
- `common-games` legacy imports (`^import com.dgphoenix`) reduced:
  - before wave: `22`
  - after wave: `0`
  - delta: `-22`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-150403-hardcut-live-batchAFAG-common-games-import-rewire22/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-common-games-dgphoenix-imports.txt`
  - `post-scan-common-games-abs-imports.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-status.txt`
  - `run-rerun1.sh`
  - `fast-gate-status-batchA-rerun1.txt`
  - `fast-gate-status-batchB-rerun1.txt`
  - `prewarm-status-rerun1.txt`
  - `validation-status-rerun1.txt`
  - `validation-summary-rerun1.txt`

## Metrics
- Baseline declarations/files: `2277`
- Reduced: `2277`
- Remaining: `0`
- Hard-cut burndown: `100.000000%`
- Project 01: `100.000000%`
- Project 02: `54.645725%`
- Core total (01+02): `77.322863%`
- Entire portfolio: `88.661431%`

## ETA refresh
- Hard-cut declaration refactor ETA: `0.0h` (complete).
- Remaining stabilization/import-normalization ETA: `~4-8h` (`~0.50-1.00` workdays), now centered on post-import type-boundary mismatches in `mp-common-games` and the known external `STEP09` smoke blocker.
