# Beta 6 Review

Branch:

- `codex/qa/7000-beta6-donorlocal-parity-pass-20260317-1210`

Base:

- `codex/qa/7000-donorlocal-benchmark-mode-20260317-1117`

Benchmark:

- `assetProvider=donorlocal`
- `mathSource=provisional`
- exact benchmark URL: `http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional`

## What materially improved vs donorlocal benchmark mode

- Benchmark launch is now deterministic. `dev:benchmark` is locked to `127.0.0.1:8081` with `--strictPort`, so QA no longer gets silent port drift between docs and smoke results.
- The top stack is materially cleaner. The redundant top-center benchmark header was removed from the default benchmark path, which lets the topper and cabinet own the composition instead of fighting layered debug text.
- The preloader feels more authored. The lockup plate, background flares, floor glow, status chip, and progress bar treatment now read closer to a premium branded screen instead of a generic loading overlay.
- The reel cabinet and bottom rail feel heavier and more intentional. Depth, glow, framing, and spacing now do more work to separate the game bed from the background.
- The buy bonus tile reads better. The copy, wrapping, and tile proportions are cleaner and less placeholder-like.
- Feature moments are stronger. Collect, boost, and jackpot now feel more staged through better topper text, lightning/coin emphasis, and stronger presentation timing on the donorlocal benchmark path.

## What still looks weak

- The top jackpot hierarchy is still not fully donor-grade. The plaque row is stronger than before but still lacks fully authored plaque art, numerals, and composition polish.
- The cabinet and control cluster still rely on vector fallback treatment in places where bespoke runtime art would carry more premium weight.
- Bonus and jackpot still need heavier authored choreography layers. The timing is better, but the sequences are still limited by available art and relatively light SFX hooks.
- Typography is cleaner but still runtime-driven. Some labels and numerals still read like high-quality placeholders rather than final product UI.

## Exact missing runtime art keys still needed

The current local donor manifest does not provide several premium runtime surfaces we would want for full parity:

- `heroUiAtlas` authored buy/plate/button surfaces for the top and side chrome
- `heroUiAtlas` authored jackpot plaque art with final numerals and richer tier-specific framing
- `heroVfxAtlas` stronger topper/plaque/fire/lightning hero overlays for premium feature moments
- dedicated authored topper/mascot composite art for the top-center lockup
- richer authored button-shell states for the bottom control cluster

## Remaining blocker assessment

Primary blocker:

- `a) missing authored art`

Secondary blockers:

- `b) choreography / timing`
- `c) audio`

Not yet a polish-only problem. Beta 6 materially improves the donorlocal baseline, but the next visible leap still depends on better authored runtime art and heavier feature-specific choreography/audio layers rather than more fallback geometry alone.
