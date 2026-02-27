# Hard-Cut M2 Wave 192A/192B + Wave 193 Report

Date (UTC): 2026-02-27
Wave group: 192A + 192B + 193
Scope: declaration-first migration in `gamecombos` + `unj.api` + `common.geoip` + `statistics` with bounded importer/class-string rewires.

## Batch Breakdown
- `W192A`: 8 declaration migrations (`ComboFeature`, `GameSessionCommonStatistics`, `Icon`, `ReelDetails`, `AbstractSharedGameState`, `ContributionResult`, `IDictionaryEntry`, `SharedGameStates`).
- `W192B`: 8 declaration migrations (`CountryRestrictionList`, `RestrictionType`, `GeoIp`, `GeoIpTest`, `RegistratorServlet`, `StatisticProcessor`, `TimeContainer`, `TimesHolder`).
- `W193`: integration and validation.

## Stabilization
- Parallel-mode intent executed as `1 worker + main` fallback due agent thread-cap while preserving strict non-overlap ownership.
- No source rollback required.
- Applied targeted Java importer/static-import rewires and one bounded non-Java class-string rewire in `web.xml`.
- Initial fast gate (`rerun1`) failed at `STEP01` due cross-module order after package move; reran with prewarm and achieved expected `STEP01-08 PASS`, `STEP09 FAIL` profile.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-121109-hardcut-m2-wave192ab-wave193-parallel-batches/`
- Fast gate:
  - rerun1: `STEP01 FAIL` (`rc=1`, cross-module compile ordering after package move)
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `16`.
- Scoped bounded rewires retained: `10`.
- Global tracked declarations/files remaining: `1178` (baseline `2277`, reduced `1099`).
- Hard-cut burndown completion: `48.265262%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `31.033157%`
  - Core total (01+02): `65.516579%`
  - Entire portfolio: `82.758289%`
- ETA refresh: ~`48.7h` (~`6.09` workdays).
