# Evidence Summary: Hard-Cut M2 Wave 280 + 281

- Timestamp (UTC): `2026-02-28 10:20-10:27`
- Scope: declaration-first migration of six `common.web` declarations plus bounded consumer rewires.
- Batch targets:
  - Batch A: `AbstractLobbyRequest`, `BasicGameServerResponse`, `CommonStatus`
  - Batch B: `JsonResult`, `MobileDetector`, `BaseAction`

## Validation Outcomes
- Fast gate batchA (`fast-gate-status-batchA-rerun1.txt`):
  - `STEP01-08 PASS`, `STEP09 FAIL` (`node ... refactor-onboard.mjs smoke`, `rc=2`)
- Fast gate batchB (`fast-gate-status-batchB-rerun1.txt`):
  - `STEP01-08 PASS`, `STEP09 FAIL` (`node ... refactor-onboard.mjs smoke`, `rc=2`)
- Full matrix (`validation-status-rerun1.txt`, `validation-summary-rerun1.txt`):
  - `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
  - `STEP09-retry1` also `rc=2`

## Canonical Blocker Profile
- Smoke failure remains the known external environment blocker:
  - `/startgame` route returns `HTTP 502` during `STEP09`.
