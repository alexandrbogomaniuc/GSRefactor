# Game 7000 Line Visualization Status

## Implemented

- Added a dedicated donor-style payline layer in `src/game/fx/PaylineOverlay.ts`.
- `MainScreen.ts` now sequences each `mathBridge.lineWins[]` entry one-at-a-time using `timingHints.lineHighlightDelayMs` and `lineHighlightDurationMs`.
- Each displayed line now shows:
  - the exact traced payline path,
  - the winning symbols on that line,
  - a line callout with line id/name, symbol key, multiplier, and payout amount,
  - the existing symbol highlight at the same time as the line trace.
- Added a DEV-only QA summary strip in `DebugOverlay.ts` showing:
  - winning line ids,
  - line multipliers,
  - total win multiplier.
- `mathPreset=normal|collect|boost|bonus|jackpot|mega` now resolves to deterministic boards that carry visible line wins, so choreography QA does not depend on RNG luck.
- `assetProvider=donorlocal` now resolves the first available local donor manifest from the wider `/Users/alexb/Documents/Dev` workspace instead of assuming the ignored donor bundle exists inside the active worktree.

## How Paths Are Derived

- The provisional math bridge remains the source of truth for DEV presentation routing.
- For each `mathBridge.lineWins[]` item:
  - `lineId`, `rowsByReel`, `positions`, `multiplier`, and `amountMinor` come from provisional math.
  - `MainScreen.resolveLinePresentations()` maps those `positions` onto the settled visible `CrazyRoosterSymbol` instances from the live slot machine.
  - `resolveFxLayerCenter()` converts each winning symbol into an `fxLayer` center point.
  - `PaylineOverlay` draws a polyline through those exact cell centers, then animates trace + glow + node pulses.

This means the rendered path is aligned to the actual settled symbols, not an approximate hard-coded overlay.

## Donorlocal QA Notes

- Main benchmark URL:
  - `http://127.0.0.1:8082/?allowDevFallback=1&assetProvider=donorlocal&mathSource=provisional`
- Deterministic proof presets now use distinct payline shapes so QA can verify the path renderer clearly:
  - `normal` -> line `5` (`DIAGONAL_DOWN`)
  - `collect` -> line `7` (`DIAGONAL_UP`)
  - `boost` -> line `6` (`DIAGONAL_DOWN_STEEP`)
  - `jackpot` -> line `8` (`DIAGONAL_UP_STEEP`)

## Still Missing Vs Donor

- We do not yet have donor-native numbered line plaques or donor-authored line-win sprite packages; this pass uses original Pixi vector traces and callout plates.
- Line sequencing is donor-inspired, but not yet audio-synced to donor-authored line-count stingers or per-line VO.
- The payout callout uses current local currency formatting from minor units; it is accurate for the dev runtime, but not yet styled with donor-native counting numerals.
- Production parity still depends on certified/GS-authoritative math envelopes; this remains a DEV-only presentation bridge.
