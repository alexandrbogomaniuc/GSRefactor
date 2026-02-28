# W262 + W263 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): final two `common` exception declaration package migrations (`WalletException`, `FRBException`).
- Bounded rewires: direct import updates in `common`, `common-wallet`, `common-gs`, and `web-gs` consumers.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 FAIL (`rc=2`).

## Stabilization Notes
- Added compatibility imports in moved declarations:
  - `WalletException` -> `CommonException` import.
  - `FRBException` -> `BonusException` import.
- Added explicit `com.abs` imports for wildcard users that reference moved symbols (`WalletException`, `FRBException`).
- No blind/global replacement was used.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
