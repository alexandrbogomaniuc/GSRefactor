# Hard-Cut M2 Wave 38 Report (BaseJsonAction)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W38-base-json-action`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.web.BaseJsonAction` -> `com.abs.casino.common.web.BaseJsonAction`

Wave touched 1 target file.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-123128-hardcut-m2-wave38-base-json-action`

## Key migration result
- Pre-scan legacy refs for scope: `1`
- Remaining legacy refs for scope: `0`
- New `com.abs` refs for scope: `1`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (reactor-aligned rebuild)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Compatibility corrections during validation
- Added explicit imports for `BaseAction` and `JsonResult` from old namespace to keep compile/runtime behavior stable while this class is migrated.

## Risk assessment
- Runtime logic risk: low.
- Isolated declaration migration with explicit compatibility imports.

## Next wave proposal
- M2 Wave 39: continue with next low-fanout declaration migration.
