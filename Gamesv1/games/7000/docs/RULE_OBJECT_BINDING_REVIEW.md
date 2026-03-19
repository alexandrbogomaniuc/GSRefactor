# Rule-Object Binding Review (Sprint Pass)

Validation branch:

- `codex/qa/7000-rule-object-binding-20260319-1157`

## What Is Now Fully Bound

- Core payline rule chain is fully bound: `paytable.lineWinRule` -> provisional line evaluator -> `PaylineOverlay` path trace -> symbol highlight -> staged line callouts.
- Collect / boost / bonus / jackpot math triggers are all routed into distinct visible reactions through `MainScreen.applyAnimationCue`, `TopperMascotController`, `LayeredFxController`, and line-tone selection.
- Buy bonus now has an explicit trigger cue path (`feature.buyBonus.enter`) tied to non-base math modes (`buy75` / `buy200` / `buy300`).
- Bell is now a dedicated runtime/paytable symbol slot (id `10`) with donor pay value `20x`, included in symbol model, reel weights, and line payout resolution.
- In-game rules/help parity surface now exists as a dedicated popup (`RulesHelpPopup`) with donor-rule sections: symbols, paylines, collect, chicken boost, bonus game, jackpots, buy bonus.

## What Remains Partial

- Jackpot presentation is still partly generic even though trigger binding is complete; plaque/numeral treatment is mixed between donorlocal art and runtime text.
- Bonus 3-spin reset logic is implemented in math, but the reset state lacks a dedicated authored runtime counter/surface.
- Pile-of-gold behavior is routed functionally (coin flights and topper-anchor reactions), but still uses runtime-composed FX rather than full authored donorlocal choreography layers.

## Exact Donorlocal Slots Still Missing

- `heroUiAtlas.jackpot-numeral-mini`
- `heroUiAtlas.jackpot-numeral-minor`
- `heroUiAtlas.jackpot-numeral-major`
- `heroUiAtlas.jackpot-numeral-grand`
- `uiAtlas.payline-plate-1..8` (authored line plates; runtime currently uses `payline-pill` fallback path)
- `uiAtlas.payline-sequence-chip`
- `heroVfxAtlas.boost-charge`
- `heroVfxAtlas.jackpot-burst`
- `uiAtlas.hud-history-shell`
- `uiAtlas.hud-settings-shell`
- `uiAtlas.hud-sound-shell`
- `openai/nanobanana symbol bell art` equivalent for non-donorlocal providers (runtime fallback currently maps Bell to BAR art)

## Next Blocker Classification

- Primary blocker: `a) missing local-only donor mappings`
- Secondary blocker: `b) missing authored runtime art`
- Remaining after slots/art: `c) choreography/polish only`
