# WOW_VFX_ARCHITECTURE

## Goal

Provide a reusable WOW/VFX orchestration layer for all slot templates while preserving GS-authoritative runtime truth.

## Canonical Modules

- Tier mapping: `packages/ui-kit/src/shell/vfx/WinPresentationTiers.ts`
- VFX orchestrator: `packages/ui-kit/src/shell/vfx/WowVfxOrchestrator.ts`
- Audio cue registry: `packages/ui-kit/src/shell/vfx/AudioCueRegistry.ts`
- Theme token source: `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`
- Premium integration point: `games/premium-slot/src/app/screens/main/MainScreen.ts`

## Presentation Inputs

The framework consumes only browser-visible sources:

- `presentationPayload.audioCues`
- `presentationPayload.animationCues`
- mapped `winAmount`
- mapped reel/symbol presentation
- resolved runtime config via `AnimationPolicyEngine`

No engine-private or server-audit fields are consumed.

## Win Tier Pipeline

1. Compute win multiplier from `winAmount / defaultBet`.
2. Classify policy tier with `AnimationPolicyEngine.classifyWinByMultiplier(...)`.
3. Map policy tier -> presentation tier (`normal | big | huge | mega`).
4. Resolve title and timing from policy + config.

No-win handling:

- zero-win rounds map to explicit `none` tier
- orchestrator short-circuits win presentation for `none` tier

## Cue Orchestration

`WowVfxOrchestrator` handles:

- audio cue dispatch (`onAudioCue` hooks)
- animation cue dispatch (`onAnimationCue` hooks)
- cue-based overrides:
  - `disable-heavy-win-fx`
  - `force-skip-presentation`

Audio cue mapping is resolved through shared registry entries (`resolveAudioCueActions`) instead of screen-local hardcoded cue branching.
`MainScreen` delegates cue execution to `applyAudioCue(...)`; screen code no longer owns cue action branching.

## Heavy-FX and Low-Perf Fallback

Heavy overlays/burst FX run only when all are true:

1. tier qualifies (`big|huge|mega` from policy)
2. low-performance mode is not enabled
3. payload cue does not disable heavy FX

Otherwise, heavy FX layers are cleared and only lightweight presentation remains.

Theme/VFX token hooks:

- `tierLabels` and `tierStyleHooks` to customize win-tier output labels/styles
- `intensity` (`low|normal|high`) to tune heavy FX usage
- `heavyFxEnabled` / `coinBurstEnabled` to hard-toggle expensive effects

## Timing Contract

Win presentation duration is config-driven via `AnimationPolicyEngine.getWinPresentationDurationMs(...)`.

This guarantees consistency with turbo/autoplay/skip timing contracts already defined in core-compliance.
