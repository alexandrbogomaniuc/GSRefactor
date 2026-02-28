# Hard-Cut M2 Wave 300 + Wave 301 Report

Date (UTC): 2026-02-28
Wave group: 300 + 301
Scope: declaration-first migration on low-fanout `sb-utils` util/string/transport surfaces with bounded stabilization and deferred high-drift boundaries.

## Batch Breakdown
- `W300` (Batch A): retained `5` declaration migrations (`GameTools`, `NumberUtils`, `ConcurrentHashSet`, `StringBuilderWriter`, `HexStringConverter`).
- `W301` (Batch B): retained `3` declaration migrations (`ITransportObject`, `InboundObject`, `TInboundObject`).
- Deferred from initial target: `2` (`ITimeProvider`, `CWError`).
- Total retained declaration migrations: `8`.

## Stabilization
- Parallel target remained `1 explorer + 2 workers + main`, but explorer/worker/awaiter spawning stayed thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failure:
  - `STEP02` compile drift in `common-wallet`: moved `CWError` introduced mixed type boundary (`com.dgphoenix` vs `com.abs`).
- `rerun2` failure:
  - after bounded rollback of `CWError`, `STEP06` compile drift in `common-gs`: moved `ITimeProvider` caused `NtpTimeProvider` incompatibility.
- `rerun3` fixes:
  - deferred `CWError` and `ITimeProvider` from this wave (rollback of declaration move + rewires).
  - kept retained declaration set and bounded transport compatibility imports:
    - `TInboundObject` imports unmoved `com.dgphoenix...TObject`.
    - `TObject` imports moved `com.abs...ITransportObject`.
  - canonical profile reached.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-132457-hardcut-m2-wave300-wave301-util-transport-leaf/`
- Fast gate batchA:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave checkpoint:
  - retained declaration migrations (`com.dgphoenix -> com.abs`): `8`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+8`
- Global tracked declarations/files remaining: `436` (baseline `2277`, reduced `1841`).
- Hard-cut burndown completion: `80.851998%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `48.630235%`
  - Core total (01+02): `74.315117%`
  - Entire portfolio: `87.157559%`
- ETA refresh: ~`17.8h` (~`2.22` workdays).
