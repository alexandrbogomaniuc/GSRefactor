# W258 + W259 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): common exception declaration package migrations.
- Bounded rewires: direct import updates in `common`, `common-gs`, and `web-gs` consumers.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: `STEP01-05 PASS`, `STEP06 FAIL` (missing explicit imports after package move).
- Fast gate batchA rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Full matrix rerun1: `PRE01-03 PASS`, `STEP06 FAIL`.
- Full matrix rerun2: stuck in `STEP09-retry1` (no log progress after start).
- Full matrix rerun3 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 FAIL (`rc=2`).

## Stabilization Notes
- Added minimal compatibility imports in moved exception declarations to preserve base-class references:
  - `CommonException`, `AccountException`, `ObjectNotFoundException`.
- Added timeout-bounded `STEP09` execution in `run-rerun3.sh` to prevent retry hang and preserve deterministic evidence completion.
- No blind/global replacement was used.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
