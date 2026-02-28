# Evidence Summary: Hard-Cut M2 Wave 294 + 295

- Timestamp (UTC): `2026-02-28 12:25-12:40`
- Scope: declaration-first migration of low/moderate-fanout `common/util/string` surfaces.

## Retained Targets
- `CollectionParser`
- `DateTimeUtils`
- `IStringSerializer`
- `MapParser`
- `MatrixUtils`
- `StringIdGenerator`

## Deferred Targets
- `StringBuilderWriter`
- `Attribute`
- `FormattedXmlWriter`
- `XmlQuota`
- `XmlWriter`

## Rerun Timeline
- `rerun1-rerun3`: failed at `PRE02/STEP03` due mixed moved/unmoved string/xmlwriter dependency drift.
- `rerun4`: failed at `STEP06` due legacy wallet wildcard import drift in `MQServiceHandler` for moved `MultiplayerExternalWallettransactionHandler`.
- `rerun5`: canonical profile reached after bounded explicit handler import fix.

## Canonical Validation Outcomes (`rerun5`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker remained unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
