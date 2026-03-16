# Game 7000 Math Model Status (Provisional)

## Scope of this lane
- This is a math-spec + simulation package for design validation and backend handoff.
- It does **not** replace GS/fixture runtime truth.
- Browser remains non-authoritative for wallet/session/outcome truth.

## Verified from donor/runtime evidence (used as fixed constraints)
- Grid: `3 reels x 4 rows`.
- Fixed paylines: `8` lines, with order currently used in runtime config.
- Bet ladder: `0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1,2,3,4,5,6,7,8,9,10,15,20,25,50,75,100,150,200`.
- Bet bounds: `minBet=0.1`, `maxBet=20000`.
- Jackpot multipliers: `mini=25x`, `minor=50x`, `major=150x`, `grand=1000x`.
- Buy tiers: `75 / 200 / 300`.
- Symbol ID model:
  - `0=EGG`
  - `1=CHERRIES`
  - `2=LEMON`
  - `3=ORANGE`
  - `4=PLUM`
  - `5=BAR`
  - `6=SEVEN`
  - `7=COIN`
  - `8=BOLT`
  - `9=ROOSTER`

## Provisional in this package
- Line paytable multipliers (`math/paytable.json`).
- Reel symbol distributions (`math/reel-strips-or-weights.json`).
- Hold-and-win landing probabilities and symbol-mix tables (`math/feature-tables.json`).
- Buy-bonus forced-entry composition (`math/buy-bonus-tables.json`).
- Jackpot tier selection frequency assumptions (`math/jackpots.json`, feature tables).

## Guessed/assumed explicitly
- Wild substitution behavior for symbol `6`.
- Bonus trigger threshold (`6+` bonus-family symbols in current provisional model).
- Collector conversion math (`collectorCount * coinSum * 0.35`) in simulation.

## What is simulated today
- Normal/base spins.
- Buy-bonus tiers (`75`, `200`, `300`).
- Output metrics:
  - RTP estimate
  - hit frequency
  - bonus frequency
  - jackpot frequency
  - win-tier distribution

## Certification/backend status
- `certified: false`.
- Current package/version: `7000-math-provisional` / `0.3.0-provisional`.
- This package is handoff-ready for backend/certified replacement, but not production-certified.

## Current known RTP / volatility status
- Current provisional base-game RTP metadata in this lane: `90.05%`.
- Current provisional volatility label in metadata: `medium-high`.
- Buy-mode RTP values are currently simulation outputs only and not calibrated/certified.

## Files in this lane
- `math/symbols.json`
- `math/paylines.json`
- `math/paytable.json`
- `math/reel-strips-or-weights.json`
- `math/feature-tables.json`
- `math/buy-bonus-tables.json`
- `math/jackpots.json`
- `math/win-thresholds.json`
- `math/math-notes.md`
- `scripts/run-math-sim.mjs`
