# Hard-Cut M2 Wave 116A/116B + Wave 117 Report

Date (UTC): 2026-02-26
Wave group: 116A + 116B + 117
Scope: batched-safe parallel migration with bounded integration rewires.

## Batch breakdown
- `W116A`: migrated 10 lobby/tournament/battleground declaration packages to `com.abs`.
- `W116B`: migrated 10 game-start declaration packages to `com.abs`.
- `W117`: integrated shared rewires (including `WEB-INF/struts-config.xml`) and validated.

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-221842-hardcut-m2-wave116ab-wave117-parallel-batches/`
- Fast gate: initial package failure on `CommonFRBStartGameForm`, rerun2 PASS.
- Full matrix: `9/9 PASS`.

## Outcome metrics
- Scoped declaration migrations: `20`.
- Global tracked declarations/files remaining: `1873` (baseline `2277`, reduced `404`).
- Hard-cut burndown completion: `17.742644%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `27.217830%`
  - Core total (01+02): `63.608915%`
  - Entire portfolio: `81.804457%`
- ETA refresh: ~`87.2h` (~`10.90` workdays).
