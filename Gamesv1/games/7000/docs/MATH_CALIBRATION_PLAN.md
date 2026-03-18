# Game 7000 Math Calibration Plan

## Objective

Keep donor-rule topology from the donor lock sprint, but bring provisional metrics into a sane development band for continued donorlocal presentation work.

## Current Bad Metrics (Baseline)

Baseline source (4 seeds, 200k rounds each):

- `math/reports/math-sim-calibration-baseline-seed-700020260318.json`
- `math/reports/math-sim-calibration-baseline-seed-700020260319.json`
- `math/reports/math-sim-calibration-baseline-seed-700020260320.json`
- `math/reports/math-sim-calibration-baseline-seed-700020260321.json`

Baseline averages:

- base RTP: `477.265%`
- base bonus frequency: `0.045%` (too low)
- base collect frequency: `21.468%` (too high)
- base boost frequency: `11.892%` (too high)
- base jackpot frequency: `3.557%` (far too high for jackpot values up to 1000x)
- buy RTP ordering was unstable/non-plausible (`buy75 > buy300 > buy200`)

## Suspected Causes

- Bonus coin jackpot attach chance was too high relative to bonus coin density.
- Super coin boost/jackpot outcomes were too frequent for base-mode symbol density.
- Base reel weights over-produced feature interactions.
- Buy-tier composition did not match tiered value intent strongly enough.
- Initial hold-and-win boost accounting in the simulator under-counted boost on guaranteed entry symbols.

## Parameters Tuned

- `math/reel-strips-or-weights.json`
  - lowered base-mode chicken/super density
  - raised bonus-coin on reels 1 and 3 enough to recover donor trigger frequency
- `math/feature-tables.json`
  - reduced jackpot attach outcome rate
  - reduced bonus-coin jackpot attach chance
  - narrowed coin value set to reduce runaway EV while preserving donor boost topology
  - adjusted hold-and-win respin landing probabilities
- `math/buy-bonus-tables.json`
  - rebalanced starting locked symbol counts and type weights per buy tier
  - increased tier separation so buy200/buy300 are stronger than buy75
  - adjusted jackpot tier overrides per buy tier
- `math/jackpots.json`
  - shifted provisional jackpot tier weights toward mini/minor for development stability
- `math/win-thresholds.json`
  - refreshed provisional win tier breakpoints for post-calibration multiplier distribution
- `scripts/run-math-sim.mjs`
  - fixed hold-and-win boost accounting so guaranteed entry symbols correctly influence boost frequency metrics

## Target Ranges For This Pass

- base RTP: `88% - 98%`
- base collect frequency: `5% - 12%`
- base boost frequency: `1% - 4%`
- base bonus frequency: `0.18% - 0.35%`
- base jackpot frequency: `0.05% - 0.20%`
- buy RTP ordering: `buy75 < buy200 <= buy300`

## Non-Goals

- No GS contract changes.
- No certified backend math claim.
- No donor-rule topology changes (trigger pattern and core donor mechanics remain locked).
