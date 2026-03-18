# Game 7000 Math Calibration Results

## Simulation Setup

Baseline reports (before tuning):

- `math/reports/math-sim-calibration-baseline-seed-700020260318.json`
- `math/reports/math-sim-calibration-baseline-seed-700020260319.json`
- `math/reports/math-sim-calibration-baseline-seed-700020260320.json`
- `math/reports/math-sim-calibration-baseline-seed-700020260321.json`

Post-calibration reports (after tuning):

- `math/reports/math-sim-calibration-pass-seed-700020260318.json`
- `math/reports/math-sim-calibration-pass-seed-700020260319.json`
- `math/reports/math-sim-calibration-pass-seed-700020260320.json`
- `math/reports/math-sim-calibration-pass-seed-700020260321.json`

Each report used:

- rounds per mode: `300000` (baseline used `200000` but baseline averages remained directionally stable)
- modes: `base`, `buy75`, `buy200`, `buy300`

## Before / After Metrics (Averages Across 4 Seeds)

Base mode:

- RTP: `477.265%` -> `92.526%`
- bonus frequency: `0.045%` -> `0.228%`
- collect frequency: `21.468%` -> `6.956%`
- boost frequency: `11.892%` -> `1.644%`
- jackpot frequency: `3.557%` -> `0.113%`

Buy modes RTP:

- buy75 RTP: `62.224%` -> `32.848%`
- buy200 RTP: `31.473%` -> `47.704%`
- buy300 RTP: `47.968%` -> `49.757%`

Buy ordering now:

- `buy75 < buy200 < buy300` (expected tier progression restored)

## What Changed

- Rebalanced base reel weights to reduce over-triggering collect/boost/jackpot while raising donor bonus trigger incidence.
- Cut jackpot attach pressure (both bonus-coin attach chance and jackpot outcome weighting).
- Reworked hold-and-win value distribution to avoid runaway base EV.
- Increased tier separation in buy tables via guaranteed symbols, type weights, landing chances, and jackpot tier overrides.
- Fixed simulator hold-and-win boost accounting on guaranteed entry symbols (metrics correctness fix).

## What Is Still Unrealistic

- Buy modes are still intentionally guaranteed bonus/collect (`100%`) because this provisional model represents deterministic buy entry intent.
- buy200/buy300 boost frequencies are `100%` due guaranteed super-chicken heavy entry design in this provisional pass.
- buy RTP levels are now ordered and stable but still provisional and not certification-grade.

## Still Requires Certified Backend Math

- Certified reel strips and exact symbol frequencies.
- Certified feature value distributions and hold-and-win transition constants.
- Certified jackpot trigger/tier frequencies.
- Certified buy-bonus EV targets per tier.
- Final certified big/huge/mega threshold definitions.
- Bell symbol inventory completion (currently donor paytable reference only, runtime slot still missing).
