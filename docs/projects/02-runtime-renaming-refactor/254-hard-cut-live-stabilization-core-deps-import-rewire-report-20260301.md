# 254 Hard-Cut Live Stabilization Core Deps Import Rewire (2026-03-01)

## Scope
Stabilization pass after hard-cut declaration completion, focused on compile boundary cleanup caused by mixed `com.dgphoenix`/`com.abs` imports across core dependency modules.

## Changes Implemented
- Executed bounded CRLF-safe import rewires (no blind global replace) using a discovered-type map:
  - `gs-server/common/src/main/java`
  - `gs-server/sb-utils/src/main/java`
  - `gs-server/utils/src/main/java`
  - `gs-server/common-promo/src/main/java`
- Applied targeted compatibility correction in `gs-server/rng` test import:
  - `rng/src/test/java/com/abs/casino/common/util/RNGTest.java`
- Installed dependency artifacts to refresh compile classpath compatibility:
  - `rng` (`mvn -DskipTests install`)
  - `sb-utils` (`mvn -Dmaven.test.skip=true install`)
  - `utils` (`mvn -Dmaven.test.skip=true install`)
  - `common-promo` (`mvn -Dmaven.test.skip=true install`)
- Verified `gs-server/common` compile recovers to PASS (`mvn -DskipTests install`).

## Validation
Canonical runner executed with standard wave profile (`run-rerun1.sh`) and evidence captured under current batch folder.

- Fast gate A: **FAIL STEP02** (`gs-server/common-wallet`, `mvn -DskipTests install`)
- Fast gate B: **FAIL STEP02** (`gs-server/common-wallet`, `mvn -DskipTests install`)
- Prewarm: **FAIL PRE02** (`gs-server/sb-utils`, `mvn -DskipTests install` testCompile)
- Validation: **FAIL PRE02** (`gs-server/sb-utils`, `mvn -DskipTests install` testCompile)
- STEP09 retry: **SKIP**

Primary failure profile now shifts from early `STEP01/PRE01` compile blockers to downstream mixed-type boundary issues in `common-wallet` and sb-utils test compilation under `-DskipTests` policy.

## Evidence
- Folder: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260301-111154-hardcut-live-stabilization-core-deps-abs-import-rewire`
- Files:
  - `run-rerun1.sh`
  - `run-rerun1-console.log`
  - `fast-gate-runner-batchA-rerun1.log`
  - `fast-gate-runner-batchB-rerun1.log`
  - `validation-runner-rerun1.log`
  - `validation-summary-rerun1.txt`

## Diff Footprint
- Targeted stabilization footprint: **333 files changed, +645/-645**
- Legacy Java package declarations in `gs-server`: **0** (`^package com.dgphoenix`)

## Metrics Snapshot
- Baseline tracked declarations/files: `2277`
- Reduced: `2277`
- Remaining: `0`
- Burndown: `100.000000%`
- Project 01: `100.000000%`
- Project 02: `54.645725%`
- Core total (01+02): `77.322863%`
- Entire portfolio: `88.661431%`
- ETA (hard-cut declarations): `0.0h (0.00 workdays)`

## Next Focus
- Resolve `STEP02` mixed-type collisions in `common-wallet` (`com.dgphoenix` vs `com.abs` type interop) using bounded interface/adapter rewires.
- Normalize sb-utils test compilation or move canonical prewarm to explicit main-only policy if testCompile is intentionally out-of-scope for hard-cut waves.
