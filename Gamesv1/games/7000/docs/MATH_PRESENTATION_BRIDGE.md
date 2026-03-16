# Game 7000 Math Presentation Bridge (DEV-ONLY)

## Purpose

This bridge connects the provisional math package under `Gamesv1/games/7000/math/` to the existing front-end presentation pipeline for choreography QA.

It is intentionally **non-authoritative**:

- Production/runtime truth remains GS/fixture envelopes.
- `mathSource=provisional` is local dev/debug only.
- No GS contract shape was changed.

## Enable Provisional Math Source

Use:

- `?allowDevFallback=1&mathSource=provisional`

Optional controls:

- `mathPreset=normal|collect|boost|bonus|jackpot|mega`
- `mathMode=base|buy75|buy200|buy300`
- `mathSeed=<number>`

Examples:

- `http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional`
- `http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional&mathPreset=collect`

## Result Shape Generated

The provisional source produces a typed math outcome (`ProvisionalMathOutcome`) and adapts it into `presentationPayload` consumed by existing UI mapper/modules.

Core generated fields:

- `reelStops`
- `symbolGrid`
- `lineWins` (in `presentationPayload.mathBridge`)
- `totalWinMultiplier` and total win in minor units
- trigger flags:
  - `collect`
  - `boost`
  - `bonus`
  - `jackpot` (+ jackpot tier/level)
- win tier classification:
  - `none`
  - `big`
  - `huge`
  - `mega`
- donor-inspired timing hints:
  - reel stop delays
  - line highlight delay/duration
  - feature cue timing windows

Bridge metadata is exposed under:

- `presentationPayload.mathBridge`

for MainScreen choreography scheduling.

## Presentation Trigger Mapping

The bridge maps outcome flags + donor trigger docs into animation/sound cues:

- Reel staging:
  - `round.reel.stop.1`
  - `round.reel.stop.2`
  - `round.reel.stop.3` or `round.reel.stop.3.bonusHold`
- Feature choreography:
  - `feature.collect.triggered`
  - `feature.boost.triggered`
  - `feature.bonus.enter`
  - `feature.jackpot.attached`
- Win sequencing:
  - `overlay.winTier.enter`
  - `overlay.totalSummary.update`

MainScreen now:

- reads `mathBridge` hints via `readMathBridgeHints`
- schedules reel-stop cues during spin
- schedules feature cues on settle
- uses `lineWins` positions for line highlight target selection
- continues feeding existing `FeatureModuleManager` and `WowVfxOrchestrator`

## Provisional vs Final

### Provisional now

- Reel/symbol outcomes from local weight tables.
- Line win evaluation from local paylines/paytable.
- Hold-and-win style bonus simulation and jackpot tier selection from provisional tables.
- Deterministic debug presets for choreography QA.

### Still requires certified/backend math

- Certified reel strips/weights by mode.
- Certified bonus trigger frequency + respin landing curves.
- Certified jackpot tier probabilities per mode.
- Certified line/non-line payout calibration for production RTP/volatility.
- Server-side authoritative envelope integration for production math parity.

## QA Presets

Use these deterministic presets:

- `mathPreset=normal`
- `mathPreset=collect`
- `mathPreset=boost`
- `mathPreset=bonus`
- `mathPreset=jackpot`
- `mathPreset=mega`

These are intended for presentation/staging validation (line highlights, topper reactions, FX layering, win-tier flow) without waiting for random outcomes.

## Runtime Control API (DevTools)

When `MainScreen` is active, use:

- `window.__game7000.math.state()`
- `window.__game7000.math.setSource("provisional")`
- `window.__game7000.math.setPreset("collect")`
- `window.__game7000.math.setMode("buy75")`
- `window.__game7000.math.clearPreset()`
- `window.__game7000.math.spinPreset("jackpot")`
- `window.__game7000.math.spinPreset("mega", "buy300")`

These helpers update query params in-place (`history.replaceState`) and then drive the existing spin pipeline, so feature choreography can be tested without page reload.
