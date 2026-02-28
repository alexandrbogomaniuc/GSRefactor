> [!WARNING]
> This document is archived and may be outdated.
> Do not use it as source of truth.
> Canonical entrypoint: `docs/MasterContext.md` and `docs/DOCS_MAP.md`.
# Performance Budget & Guidelines

## Practical "Red Flags" List
- Dropping below 55 FPS consistently during reel spins.
- Massive GC pauses (garbage collection hooks during animations).
- Using advanced filters (DropShadow, Glow, Blur) on actively scaling or moving container trees.
- Draw calls exceeding 200 in base game.
- Memory footprint exceeding 100MB on mobile.
- Long boot time (white/black screen before the preloader shows).

## Profiling Tools
- **Chrome DevTools (Performance Tab)**: Standard for identifying CPU bottlenecks, heavy layout recalculations, and scripting delays.
- **Spector.js / WebGL Insight**: Vital for debugging draw calls, overdraw, and texture memory.
- **Pixi Inspector Extension**: Great for identifying deep or overly complex display graphs.
- **Browser Memory Snapshots**: Finding lingering object references (e.g. not clearing Tweens, untracked Textures).

## Test Device / Browser Matrix
| Tier | Platform | Browser | Representative Device(s) | Expected Performance |
|---|---|---|---|---|
| Target | iOS | Safari | iPhone 12 / 13 | Solid 60 FPS
| Target | Android | Chrome | Samsung Galaxy S21 / Pixel 6 | Solid 60 FPS
| Minimum | iOS | Safari | iPhone 8 / X | 30 - 60 FPS (some framedrops acceptable limits) |
| Minimum | Android | Chrome | Samsung Galaxy S10 | 30 - 60 FPS
| Desktop | Windows/macOS | Chrome/Safari | Any modern laptop | Solid 60 FPS

