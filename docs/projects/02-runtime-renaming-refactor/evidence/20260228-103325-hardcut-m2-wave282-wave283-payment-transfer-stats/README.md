# Evidence Summary: Hard-Cut M2 Wave 282 + 283

- Timestamp (UTC): `2026-02-28 10:33-10:54`
- Scope retained:
  - declaration migrations in `common/cache/data/payment/transfer` (`+5` net).
  - bounded compatibility rewires in consumers and JSP imports.

## Rerun Timeline
- `rerun1`: failed `STEP01` (mixed payment package move produced same-package visibility/duplicate-class drift).
- `rerun2-rerun5`: failed `STEP07` (JSPC stale imports for already-moved classes).
- `rerun6`: canonical profile reached.

## Canonical Validation Outcomes (`rerun6`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker remained unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
