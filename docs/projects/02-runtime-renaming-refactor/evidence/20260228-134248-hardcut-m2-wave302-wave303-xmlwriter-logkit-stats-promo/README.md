# Evidence Summary: Hard-Cut M2 Wave 302 + 303

- Timestamp (UTC): `2026-02-28 13:42-13:54`
- Scope: declaration-first migration of low-fanout `sb-utils` `xmlwriter/logkit/statistics` surfaces.

## Initial Targets (10)
- `GameLog`
- `LogUtils`
- `ThreadLog`
- `IStatisticsGetter`
- `IntervalStatistics`
- `PromoWinInfo`
- `Attribute`
- `FormattedXmlWriter`
- `XmlQuota`
- `XmlWriter`

## Retained Targets (9)
- `GameLog`
- `LogUtils`
- `ThreadLog`
- `IStatisticsGetter`
- `IntervalStatistics`
- `Attribute`
- `FormattedXmlWriter`
- `XmlQuota`
- `XmlWriter`

## Deferred Targets (1)
- `PromoWinInfo`

## Rerun Timeline
- `rerun1`: failed at `PRE01` (`gs-server/utils`) due compile-order drift after pre-step rewires to moved `GameLog` before `sb-utils` install.
- `rerun2`: after bounded rollback in pre-step modules (`utils/common/common-wallet`), failed at `STEP06` due mixed-type wallet boundary on `PromoWinInfo` (`com.dgphoenix` vs `com.abs`).
- `rerun3`: after bounded rollback/defer of `PromoWinInfo`, canonical profile reached.

## Canonical Validation Outcomes (`rerun3`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
