# Beta 5D Review

## What Improved Vs Beta 5C

- Winning lines now drive the topper/plaque layer instead of only the board-local overlay:
  - the topper banner reacts per line,
  - the plaque row pulses by feature tone,
  - the cabinet glow and fire/lightning layer now move in sync with the line sequence.
- The donorlocal line-win pass feels more authored:
  - line plates continue to use exact payline geometry,
  - the callout now prefers atlas-backed plate/badge/chip art when available,
  - the line sequence shows stronger hierarchy between line id, multiplier, payout, and sequence count.
- Feature states are more visibly separated:
  - `collect` now reads as a payout sweep,
  - `boost` now reads as a hotter strike state with stronger topper/plaque energy,
  - `jackpot` now pushes the plaque row and topper banner harder than a generic win line,
  - `bonus` now has its own authored-style hold-and-win banner treatment instead of sharing the generic win path.
- Safe audio hooks now exist for line and feature choreography:
  - `line-win-standard`
  - `line-win-collect`
  - `line-win-boost`
  - `line-win-bonus`
  - `line-win-jackpot`
  - `feature-collect-enter`
  - `feature-boost-enter`
  - `feature-bonus-enter`
  - `feature-jackpot-enter`
  - `feature-win-tier`
- The current donorlocal proof path now shows authored-feeling timing without changing GS contracts or making browser math authoritative.

## What Still Feels Generic

- The payline path itself is still vector-rendered. The plate/badge treatment is richer now, but the path stroke has not yet been replaced with bespoke authored spline art.
- The topper and plaque reactions are stronger, but they are still runtime-driven pulses rather than a fully authored start/loop/finish animation package.
- Audio is safe and synchronized, but it still uses placeholder UI SFX mapping rather than dedicated win-line / topper / plaque stingers.
- The win-counter / big-win layer still relies on the generic shared WOW path once the line sequence hands off to tier presentation.

## Exact Missing Runtime Art Keys Still Needed

The runtime now has hooks for authored line UI, but these keys are still missing and currently fall back to safe existing frames:

- `heroUiAtlas.line-plate-standard`
- `heroUiAtlas.line-plate-collect`
- `heroUiAtlas.line-plate-boost`
- `heroUiAtlas.line-plate-bonus`
- `heroUiAtlas.line-plate-jackpot`
- `heroUiAtlas.line-badge-standard`
- `heroUiAtlas.line-badge-collect`
- `heroUiAtlas.line-badge-boost`
- `heroUiAtlas.line-badge-bonus`
- `heroUiAtlas.line-badge-jackpot`
- `heroUiAtlas.line-sequence-chip`

Audio hooks now exist, but dedicated authored assets are still missing for:

- `line-win-standard`
- `line-win-collect`
- `line-win-boost`
- `line-win-bonus`
- `line-win-jackpot`
- `feature-collect-enter`
- `feature-boost-enter`
- `feature-bonus-enter`
- `feature-jackpot-enter`
- `feature-win-tier`

## Remaining Blocker

- Mostly `a) art`, with `b) animation/choreography` as the next blocker.
- It is not `d) polish only` yet, because the runtime still needs bespoke authored line plates/badges/chips and stronger topper/plaque animation assets to fully cross from premium-coded to premium-finished.
