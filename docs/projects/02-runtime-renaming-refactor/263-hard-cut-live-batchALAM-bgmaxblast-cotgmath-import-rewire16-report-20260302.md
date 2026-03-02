# 263 - Hard-cut live batchAL+AM (bg_maxblast + cotg-math import rewire)

## Scope
Continuation stabilization wave after batchAHAIAJAK, targeting first-fail utility import drift in `bg_maxblastchampions` and `clashofthegods-math` enemy interfaces/implementations.

## What was executed
- Batch AL (`4` files): import normalization in `bg_maxblastchampions`:
  - `RNG` imports `com.dgphoenix -> com.abs`.
  - static `DateTimeUtils.toHumanReadableFormat` import `com.dgphoenix -> com.abs`.
- Batch AM (`10` files): import normalization in `clashofthegods-math` enemy lane:
  - `Pair` imports `com.dgphoenix -> com.abs`.
  - `Triple` import in `IEnemyData` `com.dgphoenix -> com.abs`.
- Total retained rewires: `16` import-only rewires across `14` files (no behavior logic edits).

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL (first-fail moved to `bg_maxblastchampions` boundary mismatches: `CommonException` throws namespace drift + one `Pair` list type-boundary mismatch)
- `mvn -pl games/clashofthegods -am -DskipTests compile`: FAIL (first-fail moved from `clashofthegods-math` to `clashofthegods` legacy `RNG` imports)

### Full canonical matrix (pre-push)
- Baseline rerun (`run-rerun1.sh`): FAIL at `STEP01/PRE01`.
- Canonical blocker is environment-level dependency resolution in this sandboxed session:
  - Maven cannot resolve external dependencies from `https://repo.maven.apache.org/maven2` (`Unknown host repo.maven.apache.org`).
- `validation-summary-rerun1.txt` and `validation-summary-rerun2.txt` both record:
  - `fast_gate_batchA FAIL STEP01`
  - `fast_gate_batchB FAIL STEP01`
  - `prewarm FAIL PRE01`
  - `validation FAIL PRE01`
  - `STEP09 retry SKIP`

## Measured movement
- `bg_maxblastchampions` targeted legacy utility imports reduced:
  - before wave: `5`
  - after wave: `0`
  - delta: `-5`
- `clashofthegods-math` enemy legacy `Pair/Triple` imports reduced:
  - before wave: `11`
  - after wave: `0`
  - delta: `-11`
- First-fail movement:
  - `cotg` compile blocker moved from `clashofthegods-math` mixed Pair/Triple boundaries to `clashofthegods` residual `RNG` imports.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-160241-hardcut-live-batchALAM-bgmaxblast-cotgmath-import-rewire16/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-bgmaxblast-dg-util-imports.txt`
  - `post-scan-bgmaxblast-abs-util-imports.txt`
  - `post-scan-cotgmath-enemies-dg-pairtriple-imports.txt`
  - `post-scan-cotgmath-enemies-abs-pairtriple-imports.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-first-fail.txt`
  - `fast-gate-status.txt`
  - `run-rerun1.sh`
  - `run-rerun2-localm2.sh`
  - `fast-gate-status-batchA-rerun1.txt`
  - `fast-gate-status-batchB-rerun1.txt`
  - `prewarm-status-rerun1.txt`
  - `validation-status-rerun1.txt`
  - `validation-summary-rerun1.txt`
  - `validation-summary-rerun2.txt`

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
- Remaining stabilization/import-normalization ETA: `~2-5h` (`~0.25-0.63` workdays), centered on:
  - `clashofthegods` residual `RNG` import rewires,
  - `bg_maxblastchampions` `CommonException`/`Pair` boundary signature normalization,
  - environment-available full canonical rerun.
