# Hard-Cut M2 Wave 162A/162B + Wave 163 Report

Date (UTC): 2026-02-27
Wave group: 162A + 162B + 163
Scope: batched-safe parallel migration with bounded integration stabilization.

## Batch Breakdown
- `W162A`: 10 declaration migrations in `cassandra.inject` tests, `payment.wallet.commonwalletmanger` tests, `controller.mqb` tests, and `common.util.compress` tests.
- `W162B`: 10 declaration migrations in `gs.singlegames.tools.cbservtools.autofinish`, `gs.managers.freegame` tests, `controller.frbonus` tests, `gs.managers.game.favorite` tests, and `util` classes.
- `W163`: bounded rewires in `ats/BotConfigInfo`, `gs/socket/mq/BattlegroundService`, and `services/mp/MPBotConfigInfoService`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-064930-hardcut-m2-wave162ab-wave163-parallel-batches/`
- Fast gate:
  - final PASS `9/9` on rerun6 (reruns 1-4 captured incremental compile-fix stabilization; targeted `web-gs` precheck rerun5 PASS).
- Full matrix:
  - PASS `9/9` on rerun1 (with pre-setup installs for `utils`, `sb-utils`, `common-promo`).

## Stabilization Notes
- `common-gs` compile failure from mixed friend `Status` lineage fixed by restoring compatibility mapping in `BGFStatusUtil`.
- `web-gs` test compile break in `GameUserHistoryInfoControllerTest` fixed by aligning service/model imports to the current mixed migrated package boundaries.
- Runtime-safety guardrail maintained: package-scoped declaration migration only; no blind global replace.

## Outcome Metrics
- Scoped declaration migrations retained: `20`.
- Scoped bounded rewires retained: `3`.
- Global tracked declarations/files remaining: `1324` (baseline `2277`, reduced `953`).
- Hard-cut burndown completion: `41.853316%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.231665%`
  - Core total (01+02): `65.115832%`
  - Entire portfolio: `82.557916%`
- ETA refresh: ~`54.6h` (~`6.83` workdays).
