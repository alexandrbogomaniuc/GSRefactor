# Trigger Behavior Alignment Plan (Post 2026-03-18)

## Scope

This plan converts the 2026-03-18+ user feedback into a strict implementation sequence:

- preserve approved donorlocal assets/preloader baseline
- improve math-condition -> trigger owner -> animation behavior mapping
- reduce effect overlap and staging ambiguity for collect/boost/jackpot

## Freeze Fence (Do Not Break)

1. Approved donorlocal folder stays locked:
   - `/Users/alexb/Documents/Dev/_worktrees/7000-beta4a-runtime-slot-system-20260310-0841/Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local`
2. Preloader baseline remains locked:
   - 4s hold
   - fixed centered loading label
   - animated trailing dots
3. GS contracts remain untouched; provisional math remains DEV benchmark only.

## Primary Problems To Solve

1. Feature overlap:
   - multiple feature cues can stack and produce mixed ownership (collect + boost + jackpot lanes competing).
2. Coin-fly visual integrity:
   - donor atlas rasterization must correctly handle rotated frames.
3. Mascot state correctness:
   - jackpot state must avoid bigwin-only eye treatment behavior.
4. Intro/scene race:
   - async feature intro loads should not re-show stale overlays after scene clear.

## Rule -> Owner -> Phase Contract

### Collect
- Math condition: `collectTriggered`
- Owner: collect feature lane
- Phases: settle -> anticipation -> coin-fly primary -> topper reaction -> release
- Suppress: boost/jackpot owner cues while collect owns scene

### Boost
- Math condition: `boostTriggered`
- Owner: boost feature lane
- Phases: settle -> anticipation -> selective lightning/reel-bed pulse -> topper/plaque reaction -> release
- Suppress: collect/jackpot overlay lanes while boost owns scene

### Jackpot
- Math condition: `jackpotTier != null`
- Owner: jackpot feature lane
- Phases: settle -> anticipation -> plaque-first callout -> controlled lightning follow-through -> release
- Suppress: collect/boost/extra overlay lanes while jackpot owns scene

### Win-tier
- Math condition: `winTier in {big, huge, mega}`
- Owner: win-tier lane only when no stronger feature owner is active
- Suppress: win-tier cue during active donor feature scenarios

## Execution Sequence

1. Implement feature cue precedence and suppression for donor feature scenarios.
2. Fix donor coin-fly rasterization for rotated atlas frames.
3. Separate jackpot mascot reaction from bigwin-only expression behavior.
4. Invalidate in-flight intro `play()` operations when `clear()` is called.
5. Re-test with donorlocal benchmark URL and collect/boost/jackpot presets.

## Acceptance Checklist

1. No asset path drift or preloader regression.
2. No overlapping jackpot/boost/collect owner lanes in one burst window.
3. Coin-fly frames render uncropped and stable through flight.
4. Jackpot state avoids bigwin-only eye presentation.
5. Donor feature intro does not reappear after scene clear due stale async completion.
