# Hard-Cut M2 Wave 22 Report (Isolated Account/Payment Models)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W22-isolated-models`
Status: `COMPLETE`

## Scope
Migrated namespace declarations:
- `com.dgphoenix.casino.common.cache.data.account.ExtendedAccountInfo` -> `com.abs.casino.common.cache.data.account.ExtendedAccountInfo`
- `com.dgphoenix.casino.common.cache.data.account.PlayerGameError` -> `com.abs.casino.common.cache.data.account.PlayerGameError`
- `com.dgphoenix.casino.common.cache.data.account.PlayerGameState` -> `com.abs.casino.common.cache.data.account.PlayerGameState`
- `com.dgphoenix.casino.common.cache.data.payment.GameSessionInfoContainer` -> `com.abs.casino.common.cache.data.payment.GameSessionInfoContainer`
- `com.dgphoenix.casino.common.cache.data.payment.ListOfLongsContainer` -> `com.abs.casino.common.cache.data.payment.ListOfLongsContainer`
- `com.dgphoenix.casino.common.cache.data.payment.LongValueContainer` -> `com.abs.casino.common.cache.data.payment.LongValueContainer`

Wave touched 6 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-104418-hardcut-m2-wave22-isolated-models`

## Key migration result
- Remaining legacy declarations for this scope: `0`
- New `com.abs` declarations for this scope: `6`

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
- Isolated declaration-only migration set (no dependent import changes).

## Next wave proposal
- M2 Wave 23: continue next low-fanout family outside `common-gs` compile boundary.
