# Hard-Cut M2 Wave 270 + Wave 271 Report

Date (UTC): 2026-02-28
Wave group: 270 + 271
Scope: declaration-first migration of low-risk `sb-utils` helper declarations (`common.web`, `common.persist`, `common.currency`) with bounded consumer rewires.

## Batch Breakdown
- `W270` (Batch A): `7` declaration migrations retained.
- `W271` (Batch B): bounded import rewires + validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Stabilization reruns:
  - `rerun1`: `PRE02` failed in `sb-utils` due mixed-package `xmlwriter` dependency (`Attribute/XmlQuota/FormattedXmlWriter` moved while `XmlWriter` stayed unmigrated).
  - `rerun2`: `STEP05` failed with `ServerLockInfo` type mismatch caused by partial lock-surface migration.
  - `rerun3`: `STEP05` still failed due stale `com.abs.common.lock` imports left in dependent modules.
  - lock/xmlwriter changes were rolled back from this wave; `rerun4` restored canonical profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-080617-hardcut-m2-wave270-wave271-mixed-lowrisk-web-xml-lock-persist/`
- Fast gate batchA:
  - rerun4: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun4: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun4: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `7`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+7`
- Global tracked declarations/files remaining: `551` (baseline `2277`, reduced `1726`).
- Hard-cut burndown completion: `75.801493%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.074260%`
  - Core total (01+02): `73.537130%`
  - Entire portfolio: `86.768565%`
- ETA refresh: ~`22.6h` (~`2.83` workdays).
