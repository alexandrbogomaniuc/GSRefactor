# DONOR Rules Lock (Game 7000)

## Scope

This document is the single source of truth for donor-target mechanics in the DEV provisional math lane.

Hard constraints preserved:

- GS contracts were not changed.
- Browser provisional math remains DEV/DEBUG only.
- GS/fixture runtime remains production source of truth.
- Donorlocal remains the visual benchmark mode.

## Locked Rule Matrix

| # | Donor rule | Implementation location | Status |
| --- | --- | --- | --- |
| 1 | Board is 3 reels x 4 rows | `math/paylines.json`, runtime layout already 3x4 | LOCKED |
| 2 | 8 fixed paylines, pay left-to-right, highest win per line | `math/paylines.json`, `math/paytable.json` (`lineWinRule`), line evaluator in provisional math + simulator | LOCKED |
| 3 | Bonus Coin appears only on reels 1 and 3 | `math/reel-strips-or-weights.json` (`symbol 7` set to `0` on reel 2), trigger logic in provisional math + simulator | LOCKED |
| 4 | Bonus starts with Bonus Coin on reels 1 and 3 + Chicken/Super Chicken on reel 2 | `math/feature-tables.json` (`baseGame.bonusTrigger`), runtime + sim trigger function | LOCKED |
| 5 | Bonus game uses only Bonus/Chicken/Super Chicken coins | `math/feature-tables.json` (`holdAndWinBonus.allowedSymbolIds` + spawn weights), provisional + sim bonus generation | LOCKED |
| 6 | Bonus starts with 3 spins and resets to 3 when Bonus/Chicken/Super lands | `math/feature-tables.json` (`entrySpins`, reset rule), provisional + sim loop logic | LOCKED |
| 7 | Super Chicken activates Collect, can trigger Boost, and in Bonus gathers Chicken values | `math/feature-tables.json` collect/boost rules, provisional + sim super-coin resolution | LOCKED (provisional weighting) |
| 8 | Chicken Coin triggers Collect in both modes | `math/feature-tables.json` collect rules, provisional + sim collect gating | LOCKED |
| 9 | Chicken Boost outcomes: x2/x3/x5/x7/x10 OR jackpot attach OR +2/+3/+5 coins | `math/feature-tables.json` boost options + weights, provisional + sim boost resolver | LOCKED (provisional weighting) |
| 10 | Jackpots Mini 25x, Minor 50x, Major 150x, Grand 1000x | `math/jackpots.json` levels, used by provisional + sim | LOCKED |
| 11 | Buy Bonus tiers: 75 / 200 / 300 with donor behavior intent | `math/buy-bonus-tables.json` (guaranteed chicken/super + auto boost on 300), provisional + sim buy entry | LOCKED (provisional composition) |
| 12 | Donor paytable values: 777=30, Bell=20, BAR=15, Watermelon=8, Grapes=8, Orange=2, Lemon=2, Plum=2, Cherries=1 | `math/paytable.json` (`donorReferencePaytable` + runtime payout map) | PARTIAL (Bell lacks dedicated runtime symbol slot) |

## Files Updated In This Sprint

- `Gamesv1/games/7000/math/paytable.json`
- `Gamesv1/games/7000/math/feature-tables.json`
- `Gamesv1/games/7000/math/buy-bonus-tables.json`
- `Gamesv1/games/7000/math/reel-strips-or-weights.json`
- `Gamesv1/games/7000/math/jackpots.json`
- `Gamesv1/games/7000/math/win-thresholds.json`
- `Gamesv1/games/7000/math/math-notes.md`
- `Gamesv1/games/7000/scripts/run-math-sim.mjs`
- `Gamesv1/games/7000/src/app/runtime/provisionalMathSource.ts`

## Simulation Rerun (Before vs After)

Seed and rounds used for both:

- `seed=700020260318`
- `roundsPerMode=100000`

Before lock report:

- `math/reports/math-sim-pre-donor-rules-20260318-1230.json`
- base `bonusFrequency=1.493%`

After lock report:

- `math/reports/math-sim-donor-rules-lock-20260318-1230.json`
- `math/reports/math-sim-latest.json`
- base `bonusFrequency=0.055%` (now trigger-gated by donor reel positions instead of old `6+ feature symbols`)
- base `collectFrequency=21.283%`
- base `boostFrequency=11.761%`

Interpretation:

- Donor trigger structure is now materially closer (especially bonus entry gating).
- Payout calibration remains provisional and currently overpays in base mode with this donor-rule data lock (expected until certified strip/value/tier calibration lands).

## Remaining Provisional / Unresolved

- Bell payout is donor-locked in reference table but still lacks a dedicated runtime symbol slot in the current 10-symbol inventory.
- Certified reel strips and exact certified feature-value/jackpot weight distributions are still missing.
- Big/huge/mega certified thresholds remain unresolved.
- RTP/volatility calibration remains provisional and not production-ready.
