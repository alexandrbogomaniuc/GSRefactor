## Beta 3 Visual Gap Report

Reference set:
- Current Beta 3 branch: `codex/qa/7000-beta3-visual-parity-20260309-1410`
- Beta 2B baseline: `qa/7000-beta2b-real-provider-rendering-20260309-0956`
- Donorlocal benchmark: local-only donor manifest/runtime under `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/`
- Donor reference screenshots/assets: `startScreen1.222f7cdf.png`, `mainBg.af5e8e37.png`, `btn_buy_bonus.1d85b9e0.png`

### Preloader Gaps
- Beta 2B still read like a generic shell preload with a flat lockup and weak loading emphasis.
- Beta 3 improves this with a BetOnline typographic lockup, stronger gold-framed plate, thicker red progress bar, and a darker premium reveal.
- Remaining gap: the preloader still uses typography/compositing rather than bespoke painted brand art, so it does not yet match donor-grade finish.

### Cabinet / Layout Gaps
- Beta 2B proved provider rendering but the stage still felt like a shell layout placed over the background.
- Beta 3 adds a top jackpot cluster, rooster hero area, stronger reel surround, left bonus tile, and right action stack to move the screen toward the donor composition.
- Remaining gap: cabinet silhouette, jackpot topper, and side panels still rely on code-built chrome plus reused provider art, not custom premium illustration.

### Controls Gaps
- Beta 2B controls were functional but read as stock shell controls with limited hierarchy.
- Beta 3 improves visual grouping by giving the left buy tile and right action state panel dedicated real estate while preserving the existing shell button stack.
- Remaining gap: the bottom HUD buttons are still generic shell buttons rather than a custom BetOnline control treatment.

### Motion / VFX Gaps
- Beta 2B already had working provider lightning and symbol rendering, but ambient motion and screen energy were modest.
- Beta 3 adds cabinet glow, ambient topper motion, jackpot bobbing, stronger preloader sheen, and a brighter lightning presentation.
- Remaining gap: no bespoke mascot animation cycle, no layered environmental particles, and no premium multi-stage feature entrance yet.

### Audio Gaps
- Beta 2B still surfaced hover/click alias failures as repeated console noise when UI sounds were missing.
- Beta 3 ports the safe audio guard so absent aliases now no-op instead of throwing, which removes the repeated `.play()` error spam.
- Remaining gap: there is still no richer premium SFX pack or bespoke audio branding; browser autoplay warnings remain expected until audio is user-initiated.

### Priority Fixes For Beta 3
- Completed: remove missing-alias hover/click exceptions and repeated console spam.
- Completed: promote NanoBanana as the visual hero provider while preserving OpenAI fallback and donorlocal benchmark flow.
- Completed: rebuild preloader composition and strengthen cabinet/topper/side-panel presentation.
- Next highest-value gap: replace code-built topper/side chrome with custom premium art and bespoke motion passes.
- Next highest-value gap: replace generic bottom controls with custom BetOnline-styled control chrome and branded SFX.
