# Evidence Summary: Hard-Cut M2 Wave 288 + 289

- Timestamp (UTC): `2026-02-28 11:39-11:47`
- Scope: declaration-first migration of wallet helper + external-handler surfaces with bounded compatibility rewires.

## Batch Targets
- Batch A:
  - `IWalletHelper`
  - `WalletHelper`
- Batch B:
  - `ExternalTransactionHandler`
  - `MultiplayerExternalWallettransactionHandler`

## Rerun Timeline
- `rerun1`: failed `STEP01` (moved `IWalletHelper` lost same-package visibility to unmoved wallet types).
- `rerun2`: canonical profile reached after bounded compatibility imports in moved declarations.

## Canonical Validation Outcomes (`rerun2`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker remained unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
