# Project Kickoff: Premium Slot Game Client

## Vision
A premium, mobile-first HTML5 slot game client that acts as a robust presentation layer and state recovery engine over a server-authoritative backend, delivering provider-grade animations and visual polish across devices.

## Tech Stack & Versions
- **Language**: TypeScript
- **Engine**: PixiJS v8.x
- **Build Tool**: Vite
- **Asset Pipeline**: AssetPack
- **Animation**: GSAP, Spine runtime (Pixi v8 compatible), Effekseer

## Folder Structure Proposal
- `engine/` (rendering, time, events, assets, audio)
- `game/slots/` (reels, symbols, wins, math bridge)
- `themes/` (art + config only)
- `scenes/` (boot, preload, base game, bonus)

## Milestones
1. **Vertical Slice**: Essential base game loop (spin, stop, basic win).
2. **Alpha**: Full game flow, all features present, initial assets integrated.
3. **Beta**: Polish phases, full VFX, performance optimized, cross-platform passing.
4. **Gold Candidate**: Passed final QA, stable 60 FPS, zero critical bugs.

## Performance Budgets (Mobile-First)
- **Target**: Stable 60 FPS on mid-tier mobile (e.g., standard iPhone, prevalent Android models).
- **Draw Calls**: Keep batching tight; avoid breaking batches unless necessary. High priority warning if > 150.
- **Initial Load**: Keep boot bundle as small as possible. Load additional assets on demand.

## Definition of Done
- Complete implementation against specs.
- Passed client-side tests, matches server state expectations.
- Zero TS errors or dev-mode warnings.
- Visuals verified smoothly on physical mobile devices.
