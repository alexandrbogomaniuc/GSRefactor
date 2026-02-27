# Hard-Cut M2 Wave 144A/144B + Wave 145 Report

Date (UTC): 2026-02-27
Wave group: 144A + 144B + 145
Scope: batched-safe parallel migration with bounded integration stabilization.

## Batch Breakdown
- `W144A`: 13 declaration migrations in `common-gs/promo/tournaments/messages`.
- `W144B`: 15 declaration migrations in `common-gs/promo/tournaments/messages` + `common-gs/battleground/messages`.
- `W145`: bounded importer rewires and compatibility stabilization rewires in `common-gs`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-041057-hardcut-m2-wave144ab-wave145-parallel-batches/`
- Fast gate:
  - rerun1..5 failed during stabilization.
  - rerun6 PASS `5/5`.
- Full matrix:
  - rerun1 PASS `9/9`.

## Stabilization Notes
- Initial blocker: stale tournament/battleground imports after declaration package hard-cut.
- Additional blockers resolved in bounded rewires:
  - `PlayerGameSettingsType` type identity mismatch (`AccountManager` import alignment).
  - `PaymentMeanType` / `PaymentMeanId` type identity mismatches (`PaymentManager` import alignment).
- Build parity fix retained for this environment:
  - `common-gs` validation command now includes `-Dcluster.properties=common.properties`.

## Outcome Metrics
- Scoped declaration migrations retained: `28`.
- Scoped bounded rewires retained: `27`.
- Global tracked declarations/files remaining: `1559` (baseline `2277`, reduced `718`).
- Hard-cut burndown completion: `31.532718%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `28.941590%`
  - Core total (01+02): `64.470795%`
  - Entire portfolio: `82.235397%`
- ETA refresh: ~`64.2h` (~`8.03` workdays).
