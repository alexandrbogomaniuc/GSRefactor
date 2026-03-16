# Game 7000 Provisional Math Notes

## Scope
- This package is for design validation, simulation, and backend handoff.
- It is explicitly **provisional** and **uncertified**.
- GS/fixture runtime remains authoritative for wallet, session, and outcome truth.

## Verified Inputs Used
- Grid: 3 reels x 4 rows.
- Fixed paylines: 8 (order from current runtime config).
- Bet ladder: `0.1 ... 200` (exact ladder in `game.settings.json`).
- Bet bounds: `minBet=0.1`, `maxBet=20000`.
- Jackpot multipliers: mini `25x`, minor `50x`, major `150x`, grand `1000x`.
- Buy tiers: `75`, `200`, `300`.
- Symbol ID map `0..9` from `CrazyRoosterGameConfig.ts`.

## Provisional / Assumed in this Lane
- Paytable values for line-paying symbols.
- Reel symbol distributions (weights) because certified strips are not present.
- Hold-and-win landing probabilities and symbol-type mix.
- Jackpot tier selection frequencies.
- Buy-bonus entry composition and weighting.

## Handoff Intent
- Backend/certified math can replace these JSON tables directly.
- Simulation harness reads the same JSON package, so diffing before/after certified input is straightforward.
