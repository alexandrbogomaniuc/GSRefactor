# 264 - Hard-cut live batchAN+AO (clashofthegods + bg_maxblast import rewire)

## Scope
Continuation stabilization wave after batchAL+AM, targeting residual legacy imports in `clashofthegods` and `bg_maxblastchampions` first-fail lanes.

## What was executed
- Batch AN (`8` files): `clashofthegods` import normalization from `com.dgphoenix` to `com.abs` for:
  - `CommonException`, `RNG`, `Pair`, `Triple`.
- Batch AO (`6` files): `bg_maxblastchampions` import normalization from `com.dgphoenix` to `com.abs` for:
  - `CommonException`, `Pair`.
- Total retained rewires: `23` import/signature-boundary bindings across `14` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL (first-fail moved to `mp-server/core` residual legacy `RNG` imports in `Member`, `BGPrivateRoomInfoService`, `MultiNodePrivateRoomInfoService`, `NicknameGenerator`, `BotManagerService`, `SingleNodeRoomInfoService`, `MultiNodeRoomInfoService`)
- `mvn -pl games/clashofthegods -am -DskipTests compile`: FAIL (first-fail moved to `EnemyGame` `GameTools.getRandomPair` Triple boundary mismatch: method expects `com.dgphoenix...Triple` while call site now uses `com.abs...Triple`)

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` unreachable), not batch-local compile semantics.

## Measured movement
- `clashofthegods` legacy imports (`CommonException|RNG|Pair|Triple`) reduced:
  - before wave: `15`
  - after wave: `0`
  - delta: `-15`
- `bg_maxblastchampions` legacy imports (`CommonException|Pair`) reduced:
  - before wave: `8`
  - after wave: `0`
  - delta: `-8`
- First-fail movement:
  - `web` shifted from `bg_maxblastchampions` override mismatch lane to narrower `mp-server/core` RNG import lane.
  - `cotg` shifted from missing legacy imports to narrowed `GameTools` Triple boundary mismatch.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-161217-hardcut-live-batchANAO-cotg-bgmax-import-rewire23/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-cotg-dg-imports.txt`
  - `post-scan-cotg-abs-imports.txt`
  - `post-scan-bgmax-dg-imports.txt`
  - `post-scan-bgmax-abs-imports.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-first-fail.txt`
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
- Remaining stabilization/import-normalization ETA: `~1.5-4h` (`~0.19-0.50` workdays), centered on:
  - `mp-server/core` residual `RNG` import rewires,
  - `GameTools` Triple boundary normalization for `clashofthegods` consumer.
