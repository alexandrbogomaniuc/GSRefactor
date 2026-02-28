# Evidence Summary: Hard-Cut M2 Wave 296 + 297

- Timestamp (UTC): `2026-02-28 12:46-12:59`
- Scope: declaration-first migration of low-fanout `sb-utils` cache/game/lock/util surfaces.

## Retained Targets
- `JsonDeserializableDeserializer`
- `JsonDeserializableModule`
- `UniversalCollectionModule`
- `ClientGeneration`
- `Html5PcVersionMode`
- `ServerLockInfo`
- `ChangeLockListener`
- `BidirectionalMultivalueMap`
- `ConcurrentBidirectionalMap`
- `EnumMapSerializer`
- `FastByteArrayOutputStream`
- `Controllable`

## Deferred Targets
- none

## Rerun Timeline
- `rerun1`: failed at `STEP01/PRE01` due early-module import rewires to moved `com.abs` classes before `sb-utils` compile/install.
- `rerun2`: failed at `STEP03/PRE02` due `sb-utils` same-module compatibility drift after full rollback (`ConcurrentBidirectionalMap` duplicate/cannot-access path).
- `rerun3`: canonical profile reached after bounded stabilization (keep `com.abs` rewires only for in-module `sb-utils` consumers, rollback external usage rewires).

## Canonical Validation Outcomes (`rerun3`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
