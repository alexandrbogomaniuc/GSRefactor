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

### 🚧 In Progress (Current Focus)
* (None - Template Baseline Complete)

### 📅 Backlog
* Testing and integrating the very first real production game (using the scaffolding script).
* Expanding the `WowFxRecommendations.md` asset library over time.

## 4. ARCHIVE / NOTES
* **Wow FX Strategy:** Preloader uses CSS `clip-path` masks for red/white dual font colors.
* **Interop:** The 🏠 Home button dispatches `window.parent.postMessage({ action: 'HOME' })`.
* **Idempotency:** Bet and Settle requests MUST use the exact same locally generated UUID `operationId`.
