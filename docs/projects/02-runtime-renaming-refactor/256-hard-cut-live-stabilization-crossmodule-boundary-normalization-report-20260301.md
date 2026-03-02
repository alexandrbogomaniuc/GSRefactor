# 256 - Hard-cut live stabilization (cross-module boundary normalization)

## Scope
Post-hard-cut stabilization attempt to clear STEP06 by normalizing remaining mixed namespace usage in already `package com.abs...` Java sources.

## What was executed
- Targeted STEP06 import rewires in `common-gs` for `GameServerConfiguration`, `MQServiceHandler`, `GameServerComponentsHelper`, `GameServer`.
- Guarded normalization pass:
  - for Java files already declared as `package com.abs...`, rewired remaining `import com.dgphoenix.casino...` to `import com.abs.casino...`.
- Dependency rebuild attempts:
  - `cassandra-cache/cache` PASS
  - `cassandra-cache/common-persisters` PASS
  - `promo/core` FAIL (cross-module type boundary mismatches)
  - `common-gs` remains FAIL with same mismatch family.

## Validation status
- Canonical status: FAIL (stabilization blocker before canonical rerun).
- Primary blockers are no longer single-file STEP06 imports; they are cross-module API boundary mismatches where `com.dgphoenix` and `com.abs` types coexist in method signatures and generic bounds.

## Evidence
- `promo-core-install.log`
- `promo-core-errors.txt`
- `common-gs-install.log`
- `common-gs-errors.txt`
- `stabilization-status.txt`

## Metrics
- Declarations baseline: `2277`
- Reduced: `2277`
- Remaining: `0`
- Burndown: `100.000000%`
- Project 02 weighted completion: `54.645725%` (unchanged this attempt)
- Core (01+02): `77.322863%`
- Entire portfolio: `88.661431%`

## ETA refresh
- Hard-cut declaration ETA remains `0.0h`.
- End-to-end Project 02 stabilization ETA revised to approximately `28-36h` (`3.5-4.5` workdays) due cross-module boundary repair scope.
