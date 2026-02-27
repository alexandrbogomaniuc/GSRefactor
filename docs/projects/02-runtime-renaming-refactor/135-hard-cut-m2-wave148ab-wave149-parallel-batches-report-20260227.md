# Hard-Cut M2 Wave 148A/148B + Wave 149 Report

Date (UTC): 2026-02-27
Wave group: 148A + 148B + 149
Scope: batched-safe parallel migration with bounded integration stabilization.

## Batch Breakdown
- `W148A`: 10 declaration migrations in `sb-utils/common/util/string/mappers` + `sb-utils/common/util/xml/xstreampool`.
- `W148B`: 10 declaration migrations in `sb-utils/common/vault` + `common/util/hardware/data`.
- `W149`: bounded importer rewires across `common`, `common-gs`, and `sb-utils`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-044917-hardcut-m2-wave148ab-wave149-parallel-batches/`
- Fast gate:
  - rerun1 failed at `common-gs` (`Initializer` import unresolved) due dependency ordering.
  - rerun2 PASS `6/6` after adding `common` install pre-step.
- Full matrix:
  - rerun1 failed at `step08` (mp-server command shape; missing reactor deps)
  - rerun2 PASS `9/9` with corrected reactor invocation (`mp-server/pom.xml -pl persistance -am`).

## Stabilization Notes
- Dependency-order alignment retained:
  - added `common` install before `common-gs` in fast gate for this wave’s migrated `hardware.data` declarations.
- Matrix command alignment retained:
  - corrected mp-server step from module-POM invocation to root reactor invocation.
- Runtime-safety guardrail maintained:
  - no global replace; only package-scoped declaration migration and bounded import rewires.

## Outcome Metrics
- Scoped declaration migrations retained: `20`.
- Scoped bounded rewires retained: `6`.
- Global tracked declarations/files remaining: `1501` (baseline `2277`, reduced `776`).
- Hard-cut burndown completion: `34.079930%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `29.259991%`
  - Core total (01+02): `64.629996%`
  - Entire portfolio: `82.314998%`
- ETA refresh: ~`61.8h` (~`7.72` workdays).
