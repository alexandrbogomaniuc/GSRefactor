# Hard-Cut M2 Wave 198A/198B + Wave 199 Report

Date (UTC): 2026-02-27
Wave group: 198A + 198B + 199
Scope: declaration-first migration in `services*` + `transactiondata*` with safe subset in `promo.exception` and `gs.api.service`.

## Batch Breakdown
- `W198A`: 8 declaration migrations (`services.gamelimits`, `services.geoip`, `services.tournament`, `services`, `transactiondata`).
- `W198B`: narrowed safe subset to 2 declarations (`UnsupportedCurrencyException`, `RESTServiceClient`) after lock-package rollback.
- `W199`: integration and validation.

## Stabilization
- Parallel-mode intent executed as `explorer + worker + main` with thread-cap fallback while preserving strict non-overlap ownership.
- Corrected mixed-namespace compile chain uncovered during reruns:
  - `MPBotConfigInfoService` import wiring corrected to `com.abs` in Spring/controller bindings.
  - `LoginService` declaration moved to `com.abs` and dependent wiring aligned.
  - `IPaymentProcessor` imports rewired to `com.abs` in payment transfer processors/factory/tracker.
  - promo message handler imports rewired from `com.dgphoenix...messages.(client.requests|server.responses)` to `com.abs...`.
  - prize notification package drift corrected back to `com.dgphoenix` in `common-promo` to restore module compile consistency.
  - `GameUserHistoryServiceTest` type bindings aligned with migrated `com.abs` service/DTO types.
  - `RESTServiceClient` + `APIServiceTest` XStream allowlist updated to `com.abs.casino.gs.api.service.xml.**`.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) out of migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-131332-hardcut-m2-wave198ab-wave199-parallel-batches/`
- Fast gate:
  - rerun8: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`; recovery retry executed once)

## Outcome Metrics
- Scoped declaration migrations retained: `10`.
- Scoped bounded rewires retained: `8` (`rewires-batchA-all.txt` + `rewires-batchB-all.txt`).
- Global tracked declarations/files remaining: `1144` (baseline `2277`, reduced `1133`).
- Hard-cut burndown completion: `49.758454%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `31.442425%`
  - Core total (01+02): `65.721213%`
  - Entire portfolio: `82.860606%`
- ETA refresh: ~`47.3h` (~`5.91` workdays).
