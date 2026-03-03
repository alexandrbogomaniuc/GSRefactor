# WIN_PRESENTATION_TIERS

## Canonical Tiers

Presentation tiers:

- `none`
- `normal`
- `big`
- `huge`
- `mega`

Policy tiers from animation policy:

- `none`
- `big`
- `huge`
- `mega`

Mapping rule:

- `winAmountMinor <= 0` -> `none`
- policy `none` + win > 0 -> `normal`
- `big` -> `big`
- `huge` -> `huge`
- `mega` -> `mega`

## Default Thresholds (Multiplier)

From `AnimationPolicySchema` defaults:

- big: `>= 10x`
- huge: `>= 25x`
- mega: `>= 50x`

Multiplier is `winAmountMinor / defaultBetMinor`.

## Titles

- none -> empty/no win counter title
- normal -> `WIN`
- big -> `BIG WIN`
- huge -> `HUGE WIN`
- mega -> `MEGA WIN`

## Timing

Timing is resolved from `AnimationPolicyEngine` and runtime config:

- none short-circuits presentation (`0ms`)
- normal uses policy `none` duration bucket
- big uses `big`
- huge uses `huge`
- mega uses `mega`

If forced-skip is active (policy or cue), duration resolves to `0`.

## Low-Performance Behavior

In low-performance mode:

- heavy overlays and burst FX are suppressed
- win counter and lightweight messaging still run
