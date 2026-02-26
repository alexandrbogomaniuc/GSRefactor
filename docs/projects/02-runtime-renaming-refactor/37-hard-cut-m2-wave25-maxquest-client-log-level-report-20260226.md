# Hard-Cut M2 Wave 25 Report (MaxQuestClientLogLevel)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W25-maxquest-client-log-level`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.bank.MaxQuestClientLogLevel` -> `com.abs.casino.common.cache.data.bank.MaxQuestClientLogLevel`

Wave touched 3 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-105534-hardcut-m2-wave25-maxquest-client-log-level`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `2`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (rerun with explicit `cluster.properties`)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Notes
- Initial Wave 25 run failed on `common` compile because `BankInfo` needed an explicit import for `PlayerGameSettingsType` after Wave 24 namespace move.
- Initial `web-gs` package run failed due missing shell property `cluster.properties`; rerun with `-Dcluster.properties=local/local-machine.properties` passed.

## Risk assessment
- Runtime logic risk: low.
- Change is a declaration/import migration with compatibility test coverage.

## Next wave proposal
- M2 Wave 26: continue next low-fanout class-string/enum family with explicit dependency scan before edits.
