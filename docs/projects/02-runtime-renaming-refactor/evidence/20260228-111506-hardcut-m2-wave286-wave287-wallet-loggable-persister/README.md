# Evidence Summary: Hard-Cut M2 Wave 286 + 287

- Timestamp (UTC): `2026-02-28 11:15-11:46`
- Scope: declaration-first migration of wallet loggable/persister surfaces with bounded compatibility rewires.

## Batch Targets
- Batch A:
  - `IWalletPersister`
  - `ILoggableResponseCode`
  - `ILoggableContainer`
  - `ILoggableCWClient`
  - `SimpleLoggableContainer`
- Batch B:
  - `WalletPersister`
  - `WalletAlertStatus`
  - `CWMType`
  - `CommonWalletStatusResult`
  - `CommonWalletWagerResult`

## Rerun Timeline
- `rerun1`: failed `STEP01` (moved status/wager results exposed wildcard-import same-package drift in legacy wallet interfaces/clients).
- `rerun2`: failed `STEP02` (mixed loggable interface package types in `common-wallet` clients).
- `rerun3`: failed `STEP06` (`GameServer` missing explicit `AccountLockedException` compatibility import).
- `rerun4`: failed `STEP07` (JSP import drift for moved `FRBWinOperationStatus` in `walletsManagerShowData.jsp`).
- `rerun5`: canonical profile reached after bounded compatibility import fixes.

## Canonical Validation Outcomes (`rerun5`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker remained unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
