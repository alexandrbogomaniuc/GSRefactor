# Hard-Cut M2 Wave 188A/188B + Wave 189 Report

Date (UTC): 2026-02-27
Wave group: 188A + 188B + 189
Scope: declaration-first migration in `sm.login` + `init` with bounded importer and class-string rewires.

## Batch Breakdown
- `W188A`: 7 declaration migrations (`BonusGameLoginRequest`, `CWLoginRequest`, `GameLoginRequest`, `LoginRequest`, `LoginResponse`, `SLLoginRequest`, `VietbetLoginRequest`).
- `W188B`: 6 declaration migrations (`ApplicationScopeNames`, `BankShutdownJob`, `DefaultConfigsInitializer`, `GsInitThread`, `QuartzInitializer`, `ShutdownFilter`).
- `W189`: integration and validation.

## Stabilization
- No source rollback required.
- Applied targeted Java importer rewires and bounded non-Java class-string rewires under `web-gs` (`web.xml`, JSPs, log4j2).
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-113123-hardcut-m2-wave188ab-wave189-parallel-batches/`
- Fast gate:
  - rerun1: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `13`.
- Scoped bounded rewires retained: `34`.
- Global tracked declarations/files remaining: `1209` (baseline `2277`, reduced `1068`).
- Hard-cut burndown completion: `46.903821%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.862977%`
  - Core total (01+02): `65.431488%`
  - Entire portfolio: `82.715744%`
- ETA refresh: ~`50.0h` (~`6.25` workdays).
