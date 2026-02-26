# M2 Wave 1 Evidence (Annotations Package)

Generated at (UTC): 2026-02-26 09:22-09:25
Wave objective: migrate `com.dgphoenix.casino.tools.annotations` references to `com.abs.casino.tools.annotations` in GS modules.

## Files
- `pre-scan-annotations-package-refs.txt` - before-state references
- `post-scan-annotations-package-refs.txt` - after-state references
- `target-files.txt` - files edited in this wave
- `build-annotations-install.txt`
- `build-kryo-validator-test.txt`
- `build-utils-test.txt`
- `build-common-promo-install.txt`
- `build-web-gs-package.txt`
- `build-mp-core-persistance-package.txt`
- `wave-summary.txt`

## Result summary
- Legacy annotation package refs in `gs-server`: `0`
- New `com.abs` annotation refs in `gs-server`: `18`
- Validation commands with `BUILD SUCCESS`: `6`

## Safety note
- No runtime behavior logic was changed.
- Only package/import strings for annotation types were migrated in this wave.
