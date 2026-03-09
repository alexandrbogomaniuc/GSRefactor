# Beta 4A Animation Architecture Plan

Scope:
- Base branch: `codex/qa/7000-beta3-visual-parity-20260309-1410`
- Sprint branch: `codex/qa/7000-beta4a-mascot-fx-system-20260309-1649`
- Hero provider: `nanobanana`
- Fallback provider: `openai`
- Benchmark only: `donorlocal`

## Goal

Beta 4A adds a real presentation system so Game 7000 can stage donor-like choreography with original assets and structured placeholders. The new layer is event-driven, provider-aware, and built to accept richer hero packs later without changing GS contracts.

## Runtime Components

### Topper / mascot controller

File:
- [TopperMascotController.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/presentation/TopperMascotController.ts)

States:
- `idle`
- `react_collect`
- `react_boost_start`
- `react_boost_loop`
- `react_boost_finish`
- `react_jackpot`
- `react_bigwin`

Current asset usage:
- real: `symbol-9-rooster`
- real: `collector-symbol`
- real: `symbol-8-bolt`
- real: `coin-multiplier-10x`
- placeholder: topper plate, aura ring, state banner

### Layered FX controller

File:
- [LayeredFxController.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/presentation/LayeredFxController.ts)

Channels:
- `fire_back`
- `fire_front`
- `lightning_path`
- `coin_fly`
- `glow_pulse`
- `topper_aura`

Current asset usage:
- real: `lightning-arc-01`
- real: `coin-multiplier-2x`
- placeholder: fire layers, stage glow, topper aura

### Win overlay choreography

File:
- [WinOverlayController.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/presentation/WinOverlayController.ts)

Stages:
- big win
- mega win
- total win

Current asset usage:
- real: `coin-multiplier-5x`
- real: `coin-multiplier-10x`
- real: `symbol-7-coin`
- placeholder: overlay backplate, bars, transition glow, text ribbons

## Reel Timing Integration

Files:
- [CrazyRoosterReel.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/slots/CrazyRoosterReel.ts)
- [CrazyRoosterSlotMachine.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/slots/CrazyRoosterSlotMachine.ts)
- [MainScreen.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/screens/main/MainScreen.ts)

New hooks:
- `onSpinStart`
- `onReelStop(reelIndex)`
- existing `onSpinComplete`

Current trigger flow:
1. `queueRuntimeEnvelope()` resolves a pending presentation plan from labels, counters, and cues.
2. `onSpinStart` resets layered FX and sets mascot to `idle`.
3. Each `onReelStop` dispatches collect, boost, and jackpot reactions.
4. `onSpinComplete` starts shell WOW presentation plus staged win overlays.
5. `finishWinPresentation()` clears overlays and returns the mascot to `idle`.

## Placeholder vs Real In Beta 4A

Real on the hero provider path:
- `nanobanana` backgrounds
- full symbol atlas
- `reel-frame-panel`
- `lightning-arc-01`
- rooster / bolt / collector / multiplier coin frames

Still placeholder-structured:
- topper plate and aura
- fire loops
- overlay plates and bars
- coin flight art treatment
- topper title lockup
- dedicated jackpot plaques
- richer win-entry bursts

## Exact Animation-Support Assets Still Missing From The Hero Provider Pack

Topper / mascot:
- `mascot-idle`
- `mascot-react-collect`
- `mascot-react-boost-start`
- `mascot-react-boost-loop`
- `mascot-react-boost-finish`
- `mascot-react-jackpot`
- `mascot-react-bigwin`
- `topper-backplate`
- `topper-aura-loop`
- `topper-title-lockup`

Layered FX:
- `fire-back-loop`
- `fire-front-loop`
- `lightning-arc-02`
- `lightning-arc-03`
- `lightning-arc-04`
- `lightning-arc-05`
- `lightning-arc-06`
- `coin-fly-sheet`
- `glow-pulse-ring`
- `collect-burst`
- `jackpot-burst`
- `spark-burst-01`
- `spark-burst-02`
- `spark-burst-03`
- `collector-ring`

Win overlays:
- `overlay-big-win`
- `overlay-mega-win`
- `overlay-total-win`
- `overlay-win-bar`
- `overlay-counter-ribbon`
- `overlay-entry-burst`
- `overlay-feature-entry`

Audio that would materially improve choreography:
- topper reaction stings
- boost start / loop / finish SFX
- collect sweep / coin flight SFX
- jackpot hit sting
- big / mega / total overlay stingers

## Proof

Proof folder:
- [beta4a-2026-03-09](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/docs/_visual_proof/beta4a-2026-03-09)
