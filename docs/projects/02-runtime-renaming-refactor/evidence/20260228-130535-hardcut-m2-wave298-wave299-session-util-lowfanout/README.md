# Evidence Summary: Hard-Cut M2 Wave 298 + 299

- Timestamp (UTC): `2026-02-28 13:05-13:15`
- Scope: declaration-first migration of low-fanout `sb-utils` session/util surfaces.

## Retained Targets
- `GameSessionExtendedProperties`
- `GameSessionStatistics`
- `IGameSession`
- `IPlayerGameSettings`
- `AccountIdGenerator`
- `DateUtils`
- `InheritFromTemplate`
- `ObjectCreator`
- `CookieUtils`
- `DESCrypter`
- `SynchroTimeProvider`
- `IGeoIp`

## Deferred Targets
- none

## Rerun Timeline
- `rerun1`: failed at `STEP03/PRE02` due moved `SynchroTimeProvider` losing same-package visibility to unmoved `ITimeProvider` and `ExecutorUtils`.
- `rerun2`: failed at same point because compatibility imports were not applied by initial patch pattern.
- `rerun3`: canonical profile reached after explicit bounded compatibility imports in moved `SynchroTimeProvider`.

## Canonical Validation Outcomes (`rerun3`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
