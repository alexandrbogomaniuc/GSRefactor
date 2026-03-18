# Beta 7 Review

Branch:

- `codex/qa/7000-beta7-donorlocal-reconstruction-20260317-1304`

Base:

- `codex/qa/7000-beta6-donorlocal-parity-pass-20260317-1210`

Benchmark:

- `http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional`

Proof helper used for clean screenshots only:

- `&mathOverlay=0`

## What materially moved closer to donor

- The preloader no longer reads like our shell with donor colors on top. It now uses donorlocal background, donorlocal wordmark/card composition, and a stronger donor-style gate layout.
- The top cluster is much closer to donor composition. The benchmark path now uses donorlocal topper art and donorlocal strike/super-strike hero reactions instead of a mostly vector badge stack.
- The reel bed is now materially closer because donorlocal uses the real donor slot-sheet crop for the bed surface instead of only generic panel framing.
- The buy bonus and bottom rail are closer to donor. `BUY BONUS`, `HOLD FOR TURBO`, and autoplay now use donorlocal art where mappings exist, and the broken white-box fallbacks were replaced with intentional placeholder shells.
- Collect, boost, and jackpot are more visibly distinct. Donorlocal ring/spark art is now in the feature stack, and the topper/plaque area reacts more clearly to those math-driven states.

## What still feels far behind donor

- The jackpot row is still not donor-grade because the current donorlocal plaque images have baked donor values, not our runtime values. It looks closer, but it is still a benchmark placeholder rather than a correct product surface.
- The top banner stack still mixes runtime text with donor art. It is cleaner than Beta 6, but it is not yet a final authored Crazy Rooster topper.
- The outer cabinet, separators, pedestal, line plates, sequence chips, and most numerals still rely on runtime geometry instead of authored donorlocal art.
- The bottom rail is materially better, but sound/settings/history and the spin shell are still placeholder-quality compared with the donor.
- Boost and jackpot choreography are stronger, but they still lack the heavier authored burst/lightning/banner stack that makes the donor feel premium.

## Exact missing donorlocal runtime mappings still needed from Donor AI

- `preloader.progress.shell`
- `preloader.progress.fill`
- `preloader.progress.head`
- `preloader.ambient.glow`
- `topper.hero.lockup` for Crazy Rooster instead of donor title art
- `topper.banner.ribbon`
- `heroUiAtlas.jackpot-plaque-mini`
- `heroUiAtlas.jackpot-plaque-minor`
- `heroUiAtlas.jackpot-plaque-major`
- `heroUiAtlas.jackpot-plaque-grand`
- separate jackpot numeral mappings so plaque art is not baked with donor values
- `heroVfxAtlas.vfx-hero-glow`
- `heroVfxAtlas.vfx-hero-pulse`
- `heroVfxAtlas.jackpot-burst`
- `uiAtlas.cabinet-frame-outer`
- `uiAtlas.cabinet-frame-inner`
- `uiAtlas.reel-separator-vertical`
- `uiAtlas.cabinet-stage`
- `uiAtlas.button-spin`
- `uiAtlas.button-sound`
- `uiAtlas.button-settings`
- `uiAtlas.button-history`
- `uiAtlas.hud-balance-plate`
- `uiAtlas.hud-bet-plate`
- `uiAtlas.hud-win-plate`
- `uiAtlas.payline-plate`
- `uiAtlas.payline-sequence-chip`
- `uiAtlas.payline-badge`
- `heroUiAtlas.boost-banner`
- `heroVfxAtlas.boost-charge`
- `heroUiAtlas.win-overlay-big`
- `heroUiAtlas.win-overlay-huge`
- `heroUiAtlas.win-overlay-mega`

## Main blocker assessment

Primary blocker:

- `a) missing donorlocal mappings`

Secondary blockers:

- `b) animation/choreography`
- `c) layout tuning`

Not a polish-only problem yet. Beta 7 materially closes the donorlocal benchmark gap, but the next visible leap still depends on donorlocal runtime surfaces that are currently missing or baked together in ways that are not safe for our final game-specific UI.
