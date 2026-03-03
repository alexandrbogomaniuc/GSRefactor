# PERFORMANCE_GUARDRAILS

## Objective

Keep premium-slot shell quality high without regressing runtime responsiveness or loading behavior.

## Guardrail Areas

1. Bundle/chunk size
2. Atlas and texture memory
3. Video overlay payload
4. Low-performance fallback behavior

## Bundle Guardrails

- Use explicit chunking strategy from `games/premium-slot/vite.config.ts`.
- Target chunks:
  - `vendor-pixi`
  - `vendor-motion`
  - `runtime-core`
  - `ui-engine`
  - app entry chunk

Rule:

- no single app chunk should carry all engine/runtime/vendor code.
- if a vendor-only chunk remains above warning threshold, track it explicitly with a mitigation owner in release notes.

## Texture + Atlas Budgets

Inherited from art pipeline baselines:

- mobile startup texture budget: <= 64 MB
- desktop startup texture budget: <= 128 MB
- mobile atlas max: 2048
- desktop atlas max: 4096

## Video Budget Guardrails

- big/huge/mega overlay assets should stay <= 8 MB each
- preload loop <= 4 MB

## Low-Performance Fallback Rules

If `animationPolicy.lowPerformanceMode` is true:

- disable heavy win FX and coin shower effects
- disable or downgrade video overlays
- keep functional win messaging/counters/HUD behavior

## Runtime Contract Safety

Performance optimizations must not alter GS-authoritative state ownership.

Allowed optimization scope:

- rendering
- asset loading
- cue orchestration

Not allowed:

- local replacement of GS financial/session/runtime truth
