# Hard-Cut M2 Wave 26 Report (IndividualGameSettingsType)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W26-individual-game-settings-type`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.bank.IndividualGameSettingsType` -> `com.abs.casino.common.cache.data.bank.IndividualGameSettingsType`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-110501-hardcut-m2-wave26-individual-game-settings-type`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `1`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Enum declaration migration with single dependent import update.

## Next wave proposal
- M2 Wave 27: continue next low-fanout enum/constants family with explicit dependency scan before edits.
