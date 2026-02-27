# Hard-Cut M2 Wave 136A/136B + Wave 137 (Stabilized) Report

Date (UTC): 2026-02-27
Wave group: 136A + 136B + 137
Scope: batched-safe parallel migration with bounded integration rewires and runtime stabilization.

## Batch Breakdown
- Planned `W136A`: 16 declarations (`websocket/tournaments/handlers` + `sb-utils/common/socket`).
- Planned `W136B`: 17 declarations (`promo/messages/handlers` + `transactiondata/storeddataprocessor`).
- Integration `W137`: bounded import rewires in owned wiring/config files.

## Stabilization Outcome
- Deferred for compatibility safety:
  - `sb-utils/common/socket` declaration hard-cut slice.
- Retained final declaration migrations (`25`):
  - `common-gs/websocket/tournaments/handlers`: `8`
  - `common-gs/promo/messages/handlers`: `8`
  - `common-gs/transactiondata/storeddataprocessor`: `9`
- Retained rewires (`4`):
  - `GameServerComponentsConfiguration`
  - `GameCommandsProcessorsConfiguration`
  - `WebSocketSessionsController`
  - `TournamentMessageHandlersFactory`

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-021132-hardcut-m2-wave136ab-wave137-parallel-batches/`
- Fast gate:
  - `rerun1` FAIL
  - `rerun2` FAIL
  - `rerun3` FAIL (smoke)
  - `rerun4` PASS (`5/5`)
- Full matrix:
  - `rerun2` FAIL (step9)
  - `rerun3` FAIL (step9)
  - `rerun4` PASS (`9/9`)

## Outcome Metrics
- Global tracked declarations/files remaining: `1642` (baseline `2277`, reduced `635`).
- Hard-cut burndown completion: `27.888450%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `28.486056%`
  - Core total (01+02): `64.243028%`
  - Entire portfolio: `82.121514%`
- ETA refresh: ~`67.7h` (~`8.46` workdays).
