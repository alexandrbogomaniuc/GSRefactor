# Phase 1A Raw Evidence Run (Chicken donor)

## What this run is
- Raw-capture checkpoint for donor investigation only.

## What it includes
- Donor launch + gameplay evidence under `assets/`.
- 60 observed spins with per-row evidence links in `OBSERVED_SPINS.csv`.
- Jackpot bet table for 3 observed bet levels in `JACKPOT_BET_TABLE.csv`.
- Console/network/perf captures and snapshots.

## What it does NOT include
- No synthesis/design docs.
- No implementation handoff.
- No asset-generation prompts.

## Video capture status
- Screencast capture worked (`assets/video/chicken_spin_short.mp4`).

## Sanitization check
- Searched run folder for `authToken=`, `token=`, `session=`, and `cookie`.
- Raw sensitive values were redacted; verification scan found no non-redacted token/session values.
