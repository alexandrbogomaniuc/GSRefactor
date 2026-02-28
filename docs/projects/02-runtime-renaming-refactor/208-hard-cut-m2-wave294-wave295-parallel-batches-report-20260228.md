# Hard-Cut M2 Wave 294 + Wave 295 Report

Date (UTC): 2026-02-28
Wave group: 294 + 295
Scope: declaration-first migration on low/moderate-fanout `sb-utils/common/util/string` surfaces with bounded stabilization.

## Batch Breakdown
- `W294` (Batch A): retained `5` declaration migrations (`CollectionParser`, `DateTimeUtils`, `IStringSerializer`, `MapParser`, `MatrixUtils`).
- `W295` (Batch B): retained `1` declaration migration (`StringIdGenerator`).
- Deferred from initial target due duplicate-type/package-visibility drift: `StringBuilderWriter`, `Attribute`, `FormattedXmlWriter`, `XmlQuota`, `XmlWriter`.
- Total retained declaration migrations: `6`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1-rerun3` failures:
  - `PRE02/STEP03` compile drift in `sb-utils` from mixed moved/unmoved dependencies in `string` subpackage.
  - fixes:
    - keep imports to already-moved `com.abs` mappers (`FromStringMapper`, `IntMapper`, `LongMapper`, `DoubleMapper`)
    - keep import to already-moved `com.abs` `CommonArrayUtils`
    - add bounded compatibility import to unmoved `com.dgphoenix` `StringUtils` in moved `StringIdGenerator`
    - defer `StringBuilderWriter` and all `xmlwriter` declarations from this wave.
- `rerun4` failure:
  - `STEP06` compile drift in `common-gs` due legacy wildcard wallet import in `MQServiceHandler` not resolving moved `MultiplayerExternalWallettransactionHandler`.
  - fix:
    - bounded explicit import `com.abs.casino.gs.managers.payment.wallet.MultiplayerExternalWallettransactionHandler` in `MQServiceHandler`.
- `rerun5` reached canonical profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-122519-hardcut-m2-wave294-wave295-string-xmlwriter-lowfanout/`
- Fast gate batchA:
  - rerun5: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun5: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun5: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `6`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+6`
- Global tracked declarations/files remaining: `467` (baseline `2277`, reduced `1810`).
- Hard-cut burndown completion: `79.490558%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `48.210786%`
  - Core total (01+02): `74.105393%`
  - Entire portfolio: `87.052696%`
- ETA refresh: ~`19.0h` (~`2.37` workdays).
