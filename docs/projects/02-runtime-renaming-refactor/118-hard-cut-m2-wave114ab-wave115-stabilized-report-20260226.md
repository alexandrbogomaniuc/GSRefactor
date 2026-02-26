# Hard-Cut M2 Wave 114A/114B + Wave 115 Report (Stabilized)

Date (UTC): 2026-02-26
Wave group: 114A + 114B + 115
Scope: execute parallel cycle, then stabilize to compatible push boundary.

## Batch breakdown
- `W114A`: migrated 11 history/protection declaration packages to `com.abs` with bounded rewires.
- `W114B`: attempted 10 login/config declaration migrations; reverted due repeated compile compatibility drift.
- `W115`: integrated stabilized A-only set and validated.

## Changed files
- Full retained file list: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-215554-hardcut-m2-wave114ab-wave115-parallel-batches/target-files.txt`

## Validation evidence
- Evidence folder: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-215554-hardcut-m2-wave114ab-wave115-parallel-batches/`
- Fast gate:
  - initial + rerun2 + rerun3 failed on compatibility drifts.
  - rerun4 passed (`web-gs package`, `refactor smoke`).
- Full matrix: `9/9 PASS`.

## Outcome metrics
- Net declaration migrations retained: `11`.
- Global tracked declarations/files remaining: `1893` (baseline `2277`, reduced `384`).
- Hard-cut burndown completion: `16.864295%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `27.108037%`
  - Core total (01+02): `63.554018%`
  - Entire portfolio: `81.777009%`
- ETA refresh: ~`87.7h` (~`10.96` workdays).
