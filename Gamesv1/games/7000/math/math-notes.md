# Game 7000 Donor-Rule Locked Math Notes

## Scope
- This package is for design validation, simulation, and backend handoff.
- It is explicitly **provisional** and **uncertified**.
- GS/fixture runtime remains authoritative for wallet, session, and outcome truth.

## Donor Rules Locked In This Sprint
- Grid: 3 reels x 4 rows.
- Fixed paylines: 8, pay left-to-right, highest win per line.
- Bonus Coin appears only on reels 1 and 3.
- Bonus trigger: Bonus Coin on reels 1 and 3 plus Chicken/Super Chicken on reel 2.
- Bonus mode symbol set: Bonus Coin / Chicken Coin / Super Chicken Coin only.
- Bonus mode spins start at 3 and reset to 3 on every landing feature coin.
- Super Chicken behavior:
  - activates Collect,
  - can trigger Chicken Boost,
  - in Bonus mode extends collection across Chicken/Super Chicken values.
- Chicken Coin triggers Collect in both modes.
- Chicken Boost outcomes: x2/x3/x5/x7/x10 multiplier, jackpot attach, or +2/+3/+5 extra bonus coins.
- Jackpot levels: mini 25x, minor 50x, major 150x, grand 1000x.
- Buy Bonus tiers: 75 / 200 / 300.
- Donor help paytable values are locked in `paytable.json` as the benchmark reference.

## Carry-Over Runtime Inputs
- Bet ladder: `0.1 ... 200` (exact ladder in `game.settings.json`).
- Bet bounds: `minBet=0.1`, `maxBet=20000`.
- Symbol ID map `0..9` from `CrazyRoosterGameConfig.ts`.

## Still Provisional / Assumed
- Bell has no dedicated runtime symbol slot in the current 10-symbol runtime inventory, so donor Bell payout remains a benchmark reference until symbol inventory expansion.
- Reel distributions are donor-rule aligned but not certified strips.
- Hold-and-win landing probabilities and symbol-value distributions remain provisional.
- Jackpot tier frequencies are provisional.
- Buy-bonus entry composition is donor-rule aligned but not certified.
- Exact donor big/huge/mega thresholds remain uncertified.

## Handoff Intent
- Backend/certified math can replace these JSON tables directly.
- Simulation harness reads the same JSON package, so diffing before/after certified input is straightforward.
