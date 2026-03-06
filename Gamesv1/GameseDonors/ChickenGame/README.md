# ChickenGame Canonical Donor Intelligence Pack

## What this donor is

- Donor title in evidence is `ChickenCoin`, launched from an `api.inout.games` launch URL that resolves to the `chicken-coin.inout.games` runtime. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt`)

## Canonical evidence location

- Phase 1A raw capture: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/`
- Phase 1C video run: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/`
- Phase 1D reconciliation (source of truth for video claims): `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/`

## Verified vs inferred

- VERIFIED: Buy Bonus is present and shows 3 purchase tiers in reconciled evidence. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`)
- VERIFIED: Autoplay capture is present in the fixed replacement clip. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/AUTOPLAY_RECONCILED.md`)
- VERIFIED: The original 20-spin claim was replaced with `video_11_twenty_spins_continuous_FIXED.mp4`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/TWENTY_SPIN_RECONCILED.md`)
- VERIFIED: How-to text documents Collect Feature, Chicken Boost Feature, Bonus Game rules, jackpot multipliers, and Buy Bonus bet-multipliers. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- INFERENCE: Pricing values shown at bet `0.6` (`45/120/180`) align with How-to multipliers (`75x/200x/300x`), i.e. multiplier * bet. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`)
- UNOBSERVED: Exact RTP, exact internal RNG weighting, exact internal jackpot weighting, and client engine internals.

## How to use this pack

1. Read `01_EXECUTIVE_SUMMARY.md` for a quick decision view.
2. Use `02_SCREEN_STATE_MAP.md`, `03_CONTROL_INVENTORY.md`, and `04_GAMEPLAY_AND_MECHANICS.md` as implementation truth.
3. Use `08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md` and `09_ASSET_GENERATION_MASTER_BRIEF.md` to produce original assets and UX.
4. Start implementation from `11_GSREFACTOR_BUILD_HANDOFF.md` and `BUILD_KICKOFF_PROMPT.md`.
