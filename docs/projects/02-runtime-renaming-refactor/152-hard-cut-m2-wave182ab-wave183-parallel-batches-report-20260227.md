# Hard-Cut M2 Wave 182A/182B + Wave 183 Report

Date (UTC): 2026-02-27
Wave group: 182A + 182B + 183
Scope: declaration-first migration in `common-gs` (`gs.biz`, `leaderboard`, and `promo.feed`) with bounded importer rewires.

## Batch Breakdown
- `W182A`: 5 declaration migrations (`GameHistory`, `GameHistoryListEntry`, `LeaderboardWinTracker`, `LeaderboardWinTrackerTask`, `LeaderboardWinUploader`).
- `W182B`: 5 declaration migrations (`AbstractFeedWriter`, `AbstractSummaryFeedWriter`, `MaxBalanceTournamentFeedWriter`, `SummaryTournamentFeedWriter`, `TournamentFeedWriter`).
- `W183`: integration and validation.

## Stabilization
- No source rollback required.
- Applied targeted importer rewires only (no global replacement).
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-101129-hardcut-m2-wave182ab-wave183-parallel-batches/`
- Fast gate:
  - rerun3: steps 1-8 PASS, step9 FAIL (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun2: `PRE01-03` PASS, `STEP01-08` PASS, `STEP09` FAIL (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `10`.
- Scoped bounded rewires retained: `5`.
- Global tracked declarations/files remaining: `1244` (baseline `2277`, reduced `1033`).
- Hard-cut burndown completion: `45.366711%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.670839%`
  - Core total (01+02): `65.335419%`
  - Entire portfolio: `82.667710%`
- ETA refresh: ~`51.5h` (~`6.44` workdays).
