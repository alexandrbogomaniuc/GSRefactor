# W264 + W265 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): 11 declaration package migrations in `common/transactiondata` from `com.dgphoenix` to `com.abs`, excluding high-fanout `ITransactionData`.
- Batch B bounded rewires (`target-batchB.txt`): explicit consumer import rewires and compatibility imports.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Stabilization Notes
- Parallel subagent mode was attempted but blocked by thread cap (`agent thread limit reached`); strict ownership-safe fallback executed on main.
- `rerun1` failed at `STEP01` (`cannot access TrackingState`) because `ITransactionData` needed explicit imports for moved `TrackingState/TrackingInfo`.
- `rerun2` failed at `STEP01` because moved declarations still needed explicit import of unmigrated `ITransactionData`.
- Applied bounded fixes only; no blind/global replacement.

## Validation (Canonical)
- Fast gate batchA rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Fast gate batchB rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Full matrix rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 FAIL (`rc=2`).

## Known External Blocker
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
