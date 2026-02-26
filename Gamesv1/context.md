# PROJECT: 3rd Party Slot Game Integration Template
# DOCUMENT: Master Context & Source of Truth

## 1. VISION
> A reusable, compliance-ready HTML5 / PixiJS template for 3rd party slot games that automatically implements the strict `abs.gs.v1` WebSocket protocol (supporting financial idempotency) and premium "wow" factor animations.

## 2. TECH STACK & ENVIRONMENT
* **OS:** Windows / PowerShell
* **Languages/Tools:** TypeScript, Pixi.js v8, Vanilla HTML/CSS, Node/Vite.
* **Network Protocol:** `abs.gs.v1` over secure WebSocket (Canonical Message Envelopes).

## 3. STATUS
### ✅ Completed
* Built `master-game-config.json` limit configs and `config-ui.html` math model tuner.
* Implemented `GSWebSocketClient.ts` with auto-reconnect, `SESSION_SYNC`, and strict schema validation.
* Built local engine state machine in `SlotEngine.ts` mapping server results to local UI evaluations.
* Frontend: BGaming-inspired responsive layout, Paytable modals, Certification HUD.
* Interactive Logic: Autoplay loops, Turboplay speed overrides, Big/Huge/Mega Win cascading animations.
* Setup Documentation: Created scaffolding powershell script and Integration Developer Guide.
* Dynamic game grid configurations (3x3, 5x3).
* Interop redirects and Free Round Bonus UI.
* Configured the MCP Google Developer Knowledge integration.
* **[premium-slot-client] Scaffolded new PixiJS v8 project + Vite.**
* **[premium-slot-client] Implemented Vertical Slice containing config-driven staggered reels and UI orchestration.**
* **[premium-slot-client] Completed `/perf_audit`**: 
    - Reduced draw calls using 1 global masking matrix instead of 5 individual reel masks.
    - Optimized Symbol GPU redraws by replacing dynamic vectors with colored pre-drawn primitives (`this.bg.tint`).
    - Implemented `GraphicsContext` object pooling for Win Particles to stop cyclic garbage collection spikes.
    - Attached native `DebugOverlay` with FPS and screen resolution trackers. 

### 🚧 In Progress (Current Focus)
* [premium-slot-client] Migrating towards Alpha Milestone (Provider-Grade Quality).

### 📅 Backlog
* **Spine Integration**: Hook up `@esotericsoftware/spine-pixi-v8` for major symbols and premium big-win overlays.
* **AssetPack + Compressed Textures**: Transition from raw web graphics to KTX2 / Basis atlas compression to eliminate mobile VRAM crashes.
* **Effekseer VFX**: Evaluate `effekseer` particle magic for advanced 3D win lines. 
* Testing and integrating the very first real production game (using the scaffolding script).
* Expanding the `WowFxRecommendations.md` asset library over time.

## 4. ARCHIVE / NOTES
* **Wow FX Strategy:** Preloader uses CSS `clip-path` masks for red/white dual font colors.
* **Interop:** The 🏠 Home button dispatches `window.parent.postMessage({ action: 'HOME' })`.
* **Idempotency:** Bet and Settle requests MUST use the exact same locally generated UUID `operationId`.
* **Agent Rule:** All changes sent to the agent need to be placed in folder `e:\Dev\GSRefactor\Gamesv1` not to the upper folders.
