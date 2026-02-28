# Evidence Summary: Hard-Cut M2 Wave 284 + 285

- Timestamp (UTC): `2026-02-28 10:58-11:07`
- Scope: declaration-first migration of four wallet-abstraction declarations plus bounded compatibility rewires.

## Batch Targets
- Batch A:
  - `AbstractWallet`
  - `AbstractWalletOperation`
- Batch B:
  - `WalletOperationInfo`
  - `WalletOperationAdditionalProperties`

## Rerun Timeline
- `rerun1`: failed `STEP01` (same-package visibility drift after package moves).
- `rerun2`: canonical profile reached after adding minimal compatibility imports in moved declarations.

## Canonical Validation Outcomes (`rerun2`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker remained unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
