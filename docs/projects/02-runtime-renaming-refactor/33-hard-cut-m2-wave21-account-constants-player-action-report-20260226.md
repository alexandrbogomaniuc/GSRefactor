# Hard-Cut M2 Wave 21 Report (AccountConstants + PlayerAction)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W21-account-constants-player-action`
Status: `COMPLETE`

## Scope
Migrated namespace declarations:
- `com.dgphoenix.casino.common.cache.data.account.AccountConstants` -> `com.abs.casino.common.cache.data.account.AccountConstants`
- `com.dgphoenix.casino.common.cache.data.account.PlayerAction` -> `com.abs.casino.common.cache.data.account.PlayerAction`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103948-hardcut-m2-wave21-account-constants-player-action`

## Key migration result
- Remaining legacy declarations for this scope: `0`
- New `com.abs` declarations for this scope: `2`

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
- Isolated declaration-only migration with no dependent import changes.

## Execution note
- Initial Wave 21 attempt on `WalletOperationInfo` was aborted due compile dependency chain (`WalletOperationStatus`/`WalletOperationType`).
- Wave 21 was re-scoped to isolated account constants/actions to preserve green matrix progression.

## Next wave proposal
- M2 Wave 22: continue next low-fanout family outside `common-gs` compile boundary.
