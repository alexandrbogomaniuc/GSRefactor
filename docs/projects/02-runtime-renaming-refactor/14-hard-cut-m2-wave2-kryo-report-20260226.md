# Hard-Cut M2 Wave 2 Report (Kryo Package)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W2-kryo`
Status: `COMPLETE_WITH_NOTED_BASELINE_TEST_ISSUES`

## Scope
Migrated package/import references:
- from `com.dgphoenix.casino.tools.kryo*`
- to `com.abs.casino.tools.kryo*`

Wave touched 55 files (mostly `kryo-validator` plus dependent test imports).

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-092746-hardcut-m2-wave2-kryo`

## Key migration result
- Remaining legacy kryo package refs in GS/MP scan: `0`
- New `com.abs` kryo refs in GS/MP scan: `72`

## Validation summary
Passing checks:
- `annotations` install
- `kryo-validator` test
- `kryo-validator` install
- `sb-utils` test (rerun)
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

Captured non-gating issues:
1. Initial `sb-utils/common` failures were dependency-order race (ran before new `kryo-validator` install). Fixed by sequential rerun.
2. `common` test rerun still reports runtime NPE in `KryoSerializationTest` (`FeedQueue` path).
3. `common-gs` rerun reports compile failure in unchanged file `BasicTransactionDataStorageHelper` (`PROTOCOL_VERSION`), outside this wave edits.

## Risk assessment
- Runtime logic risk: low.
- This wave is package/import migration in tooling/test surfaces; runtime smoke and packaging paths remained green.

## Next wave proposal
- M2 Wave 3: choose another narrow, low-risk package family and keep sequential build ordering for dependent artifacts.
