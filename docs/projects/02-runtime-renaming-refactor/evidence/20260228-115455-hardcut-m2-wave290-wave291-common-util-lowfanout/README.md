# Evidence Summary: Hard-Cut M2 Wave 290 + 291

- Timestamp (UTC): `2026-02-28 11:54-12:03`
- Scope: declaration-first migration of low-fanout `common.util` surfaces with bounded usage rewires.

## Batch Targets
- Batch A:
  - `NtpSyncInfo`
  - `LookAheadReader`
- Batch B:
  - `RSACrypter`
  - `ZipUtils`

## Rerun Timeline
- `rerun1`: canonical profile reached directly (no additional stabilization reruns required).

## Canonical Validation Outcomes (`rerun1`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker remained unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
