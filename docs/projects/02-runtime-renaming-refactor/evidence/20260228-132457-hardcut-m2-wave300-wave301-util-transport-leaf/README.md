# Evidence Summary: Hard-Cut M2 Wave 300 + 301

- Timestamp (UTC): `2026-02-28 13:24-13:36`
- Scope: declaration-first migration of low-fanout `sb-utils` util/string/transport surfaces.

## Initial Targets (10)
- `GameTools`
- `NumberUtils`
- `ConcurrentHashSet`
- `ITimeProvider`
- `CWError`
- `StringBuilderWriter`
- `HexStringConverter`
- `ITransportObject`
- `InboundObject`
- `TInboundObject`

## Retained Targets (8)
- `GameTools`
- `NumberUtils`
- `ConcurrentHashSet`
- `StringBuilderWriter`
- `HexStringConverter`
- `ITransportObject`
- `InboundObject`
- `TInboundObject`

## Deferred Targets (2)
- `ITimeProvider`
- `CWError`

## Rerun Timeline
- `rerun1`: failed at `STEP02` due mixed-type drift in `common-wallet` after `CWError` move (`com.dgphoenix` vs `com.abs`).
- `rerun2`: after bounded rollback of `CWError`, failed at `STEP06` due mixed-type drift on `ITimeProvider` boundary (`NtpTimeProvider` incompatibility).
- `rerun3`: after bounded rollback of `ITimeProvider`, canonical profile reached.

## Canonical Validation Outcomes (`rerun3`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
