# Hard-Cut M2 Wave 214A/214B + Wave 215 Report

Date (UTC): 2026-02-27
Wave group: 214A + 214B + 215
Scope: declaration-first migration in overlap-safe runtime/config surfaces with bounded rewires and per-batch fast gates.

## Batch Breakdown
- `W214A`: 10 declaration migrations (hardware, payment tracker, kafka config, logout tracker, remotecall surfaces).
- `W214B`: 10 declaration migrations (KPI, MQ history, bet persister, battleground config, system/session interfaces, common-web, online stats).
- `W215`: bounded rewires + integration validation.

## Stabilization
- Execution mode: `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires retained to planned lists (`18 + 18`).
- Stabilization fixes applied:
  - `STEP01`: added explicit `BaseAction` import in `InvalidPathStrutsActionExceptionHandler`.
  - `STEP06`: aligned mixed type families for `BattlegroundConfig` and `IRemoteCall` across common-gs remotecall/mq surfaces.
- Validation runner correction:
  - rerun4 used wrong `STEP07` path (`gs-api`) and was marked non-canonical.
  - rerun5 re-executed with required `STEP07=web-gs` path.
- No blind/global replacement performed.
- Unrelated local runtime edits (`cluster-hosts.properties`, `.tmp-w202-*`) preserved outside commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-215545-hardcut-m2-wave214ab-wave215-parallel-batches/`
- Fast gate:
  - rerun1: batchA `STEP01 FAIL`, batchB `STEP01 FAIL`
  - rerun2: batchA `STEP06 FAIL`, batchB `STEP06 FAIL`
  - rerun3: batchA `STEP06 FAIL`, batchB `STEP06 FAIL`
  - rerun4 (non-canonical path drift): batchA/batchB `STEP07 FAIL` (`gs-api` path)
  - rerun5 (canonical): batchA/batchB `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01 FAIL`
  - rerun2: `PRE01-03 PASS`, `STEP06 FAIL`
  - rerun3: `PRE01-03 PASS`, `STEP06 FAIL`
  - rerun4 (non-canonical path drift): `PRE01-03 PASS`, `STEP07 FAIL` (`gs-api` path)
  - rerun5 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Scoped declaration migrations retained: `20`.
- Scoped bounded rewires retained: `36`.
- Global tracked declarations/files remaining: `966` (baseline `2277`, reduced `1311`).
- Hard-cut burndown completion: `57.575757%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `36.382192%`
  - Core total (01+02): `68.191096%`
  - Entire portfolio: `84.095548%`
- ETA refresh: ~`39.9h` (~`4.99` workdays).
