# M2 Wave 2 Evidence (Kryo Package)

Generated at (UTC): 2026-02-26 09:27-09:31
Wave objective: migrate `com.dgphoenix.casino.tools.kryo*` to `com.abs.casino.tools.kryo*`.

## Contents
- `pre-scan-kryo-package-refs.txt`
- `post-scan-kryo-package-refs.txt`
- `target-files.txt`
- `build-annotations-install.txt`
- `build-kryo-validator-test.txt`
- `build-kryo-validator-install.txt`
- `build-sb-utils-test.txt`
- `build-sb-utils-test-rerun.txt`
- `build-common-test.txt`
- `build-common-test-rerun.txt`
- `build-common-gs-test.txt`
- `build-common-gs-test-rerun.txt`
- `build-promo-persisters-install.txt`
- `build-common-persisters-install.txt`
- `build-cache-test.txt`
- `build-web-gs-package.txt`
- `build-mp-core-persistance-package.txt`
- `runtime-smoke.txt`
- `wave-summary.txt`

## Outcome
- Legacy kryo package refs (`com.dgphoenix.casino.tools.kryo`) in GS/MP scans: `0`
- New target refs (`com.abs.casino.tools.kryo`) in GS/MP scans: `72`
- Runtime smoke and core packaging checks: PASS

## Validation notes
- Initial sb-utils/common failures were dependency-order race before `kryo-validator` was installed with new package classes.
- Rerun after `kryo-validator install` fixed sb-utils compile path.
- Common test still has an existing runtime NPE path (`FeedQueue`) and common-gs has an existing compile issue in unchanged file (`BasicTransactionDataStorageHelper`) captured for transparency.
