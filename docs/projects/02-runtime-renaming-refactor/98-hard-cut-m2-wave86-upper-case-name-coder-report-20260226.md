# Hard-Cut M2 Wave 86 Report (UpperCaseNameCoder)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W86-upper-case-name-coder`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.response.UpperCaseNameCoder` -> `com.abs.casino.actions.api.response.UpperCaseNameCoder`

Runtime/config updates:
- `APIResponseBuilder` now imports `com.abs...UpperCaseNameCoder` for cross-package compatibility.

Wave touched 2 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/UpperCaseNameCoder.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-173016-hardcut-m2-wave86-upper-case-name-coder`

## Key migration result
- Pre-scan legacy refs for wave scope: `1`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `2`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (with explicit `-Dcluster.properties=common.properties`)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Serialization naming behavior is unchanged; only package namespace and import bridge were updated.

## Next wave proposal
- M2 Wave 87: migrate next low-fanout class in `actions/api/response` package.
