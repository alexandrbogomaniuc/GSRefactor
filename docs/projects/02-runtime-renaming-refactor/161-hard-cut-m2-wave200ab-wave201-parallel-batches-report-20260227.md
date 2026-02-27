# Hard-Cut M2 Wave 200A/200B + Wave 201 Report

Date (UTC): 2026-02-27
Wave group: 200A + 200B + 201
Scope: declaration-first migration in `cbservtools.commands.*`, `common-promo.feed/network`, `configuration.observable`, and bounded utility/test surfaces.

## Batch Breakdown
- `W200A`: 10 declaration migrations in `gs.singlegames.tools.cbservtools.commands.processors*` and `commands.responses*`.
- `W200B`: 10 declaration migrations in `common-promo.feed/network`, `services.geoip` test, `configuration.resource.observable`, `common.jackpot`, and `log4j2specific` utilities.
- `W201`: integration and validation.

## Stabilization
- Primary mode executed as `1 explorer + 2 workers + main` with strict non-overlapping ownership.
- Zero-overlap selection validated up-front (`decl overlap=0`, `rewire overlap=0`, cross-overlap=0).
- Bounded rewires retained only in importer/FQCN call-sites directly referencing migrated symbols:
  - `GameCommandsProcessorsConfiguration`, command processor imports, `DBLink`, `AbstractFeedWriter`, `GameServerServiceConfiguration`, `Configuration`, `FileObserveFactory`, `ReflectionUtilsCompatibilityTest`, `GameLogger`.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) out of migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-144036-hardcut-m2-wave200ab-wave201-parallel-batches/`
- Fast gate:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`; recovery retry executed once)

## Outcome Metrics
- Scoped declaration migrations retained: `20`.
- Scoped bounded rewires retained: `16` (`rewires-batchA-all.txt` + `rewires-batchB-all.txt`).
- Global tracked declarations/files remaining: `1124` (baseline `2277`, reduced `1153`).
- Hard-cut burndown completion: `50.636803%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `31.997455%`
  - Core total (01+02): `65.998728%`
  - Entire portfolio: `82.999364%`
- ETA refresh: ~`46.5h` (~`5.81` workdays).
