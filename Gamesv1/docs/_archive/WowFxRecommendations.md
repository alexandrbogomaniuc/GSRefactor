> [!WARNING]
> This document is archived and may be outdated.
> Do not use it as source of truth.
> Canonical entrypoint: `docs/MasterContext.md` and `docs/DOCS_MAP.md`.
# Master Instruction Spec for Web Slots UI “Wow FX”

This document contains recommendations for building a premium, production-quality web game UI with high-end “wow” effects (wins, transitions, symbol highlights, particles, glow) that runs smoothly across desktop and mobile browsers.

## 1. Engine Strategy
- **PixiJS** is the preferred engine for slots UI and FX-heavy 2D rendering due to custom control over the scene graph.
- The game must be implemented as a renderer layer (PixiJS) + domain logic layer (game state, wallet calls, round resolution simulation).
- Visuals must be decoupled from logic to easily swap layouts for mobile/desktop.

## 2. Required Architecture
### Directory Layout
- `src/`
  - `app/` (boot, config, device detection)
  - `engine/` (Pixi wrappers, scene manager, ticker, resize system)
  - `scenes/` (Preload, Game, Overlay)
  - `ui/` (layout, components, fx, particles, shaders, post, timelines)
  - `systems/` (audio, input, metrics)

### Scene Management
- **BootScene**: sets renderer, detects device tier, selects variant.
- **PreloadScene**: loads atlases, fonts, audio, shader sources; shows minimal loader.
- **GameScene**: reels + main stage + core UI.
- **OverlayScene/UI Layer**: modals, tooltips, win overlays, transitions.

### Layout vs. Art
- All UI elements must be placed via a layout engine (anchors, responsive rules), not hard-coded pixel positions.

## 3. Cross-Platform Rendering Rules
- **Pixel Ratio**: Support `devicePixelRatio` but clamp for performance (e.g., `min(devicePixelRatio, tier.maxDPR)`).
- **Textures**: Use atlases. Avoid > 4096px textures. Use compressed textures or optimized WebP.
- **Filters**: Glow/bloom should be applied to small regions or toggled only during "wow moments". Use baked glows in art for static states.

## 4. Responsive Design System
- **Constraint-based layout**: Use anchors (left/right/top/bottom) and safe area support (notches).
- **Scaling**: HUD density scales based on device. Mobile uses larger buttons and fewer simultaneous UI elements.
- **Game Area**: Reel grid stays centered. Controls move to the bottom bar (portrait mobile) or side panels (desktop/landscape).

## 5. Device Detection & Tiering
- Compute a "device tier" (LOW, MID, HIGH) based on User Agent, WebGL support, and memory heuristics.
- Each tier sets `maxDPR`, `maxParticles`, `postFXEnabled`, and shader quality.

## 6. “Wow FX” Library Plan
- **Particle System**: GPU-friendly, strict object pooling, sprite-based with additive blend. Adaptive emission based on tier.
- **Shaders**: Sweep/shine, dissolve reveal, glow pulse. Keep uniforms minimal.
- **Impact Toolkit**: Subtle screen shake, flash, chromatic split (high-tier), vignette pulse.
- **Typography Win Counters**: Eased counting (fast start, slow finish), milestones with mini pops, stroke+glow.

## 7. Animation Timeline System
- Strict deterministic timeline orchestrator supporting `sequence`, `parallel`, `wait`, `tween`, `callbacks`.
- Must be cancelable for skip/fast-forward features.
- **Key Timelines**: SpinStart, SpinStop, LineWin, BigWin.
- **Timing Recipe**: Anticipation (200-500ms) -> Impact (50-120ms) -> Afterglow (600-1500ms).

## 8. Performance Budget
- **Object Pooling**: Mandatory for particles, coins, transient glows, and win highlights.
- **Mobile Safari**: Audio requires user gesture unlock. Limit memory usage. Don't use heavy postFX on low-tier devices.

## 9. Key "Wow" Recipes
- **Symbol Landing Impact**: Quick scale punch (1.0 -> 1.08 -> 1.0), expanding impact ring, tiny spark particles.
- **Line Win Highlight**: Dim background, glow pulse on symbols, draw win line, micro sparkle drift.
- **Big Win Sequence**: Background vignette, animated rays, scale pop + glow on text, coined fountain burst, confetti, screen shake on milestones.

