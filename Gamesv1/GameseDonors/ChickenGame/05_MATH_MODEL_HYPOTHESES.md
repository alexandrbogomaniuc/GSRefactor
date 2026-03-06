# 05 Math Model Hypotheses

## Scope
- This file contains hypotheses only. No hidden donor math is claimed as fact.

## Hypotheses
- HYPOTHESIS 1: Buy Bonus cost scales linearly with current bet using tier multipliers (`75x/200x/300x`).
  - Basis: How-to multiplier tiers and reconciled bet `0.6` price examples (`45/120/180`).
  - Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`

- HYPOTHESIS 2: Collect payout in base/bonus is additive over visible coin-value contributors in the triggering spin.
  - Basis: How-to text explicitly describes value summation across Bonus/Chicken/Super Chicken coins.
  - Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`

- HYPOTHESIS 3: Super Chicken Coin raises volatility by conditionally adding multiplier/jackpot/extra-coin outcomes.
  - Basis: Chicken Boost Feature description plus observed `x3` state in early readable spin rows.
  - Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/OBSERVED_SPINS.csv`

- HYPOTHESIS 4: Bonus-game continuation is persistence-driven (3-spin reset on coin land), producing streak-length variance by coin frequency.
  - Basis: How-to text states reset-to-3 behavior on qualifying coin landings.
  - Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`

## Open questions (still unknown)
- UNOBSERVED: exact trigger probabilities for each Chicken Boost outcome branch.
- UNOBSERVED: exact jackpot weighting model across MINI/MINOR/MAJOR/GRAND.
- UNOBSERVED: exact reel-strip math and symbol distribution.
- UNOBSERVED: exact EV differences among Buy Bonus tiers.
