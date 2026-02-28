> [!WARNING]
> This document is archived and may be outdated.
> Do not use it as source of truth.
> Canonical entrypoint: `docs/MasterContext.md` and `docs/DOCS_MAP.md`.
# Sprint Plan: Gamesv1 Template Implementation

## Sprint 1: Boilerplate, Tooling & Networking Base
**Duration:** Week 1 
**Focus:** Scaffold the PixiJS environment and build the robust GS connection logic.
- [ ] Initialize NPM project with Vite/Webpack for optimized bundling.
- [ ] Setup ESLint, Prettier, and TypeScript/JSDoc configuration.
- [ ] Implement `GSWebSocketClient.js`: Connection logic, `AuthToken` passing, and WSS error handling.
- [ ] Subagent Task: Verify JSON Schema payload syntax (`abs-gs-v1-envelope.schema.json`).
- [ ] Integrate mock server to listen and respond (PING/PONG, GAME_READY).

## Sprint 2: The Core Game Loop & FinState Machine
**Duration:** Week 2
**Focus:** Implement the strictly idempotent "Wager -> Spin -> Settle" lifecycle.
- [ ] Implement local state machine (`INIT`, `READY`, `RESERVED`, `EVALUATING`, `SETTLED`).
- [ ] Build `SlotEngine.js` class: Spin method generates `operationId` -> Triggers `BET_REQUEST`.
- [ ] Logic to pause reels internally while awaiting `BET_ACCEPTED`.
- [ ] Interpret deterministic reel-stop results and trigger visual alignment.
- [ ] Fire `SETTLE_REQUEST` on visual completion. Update balance UI on `SETTLE_ACCEPTED`.
- [ ] Test Harness: Subagent to inject network drops during `RESERVED` state and verify `RECONNECT_REQUEST` recovery logic.

## Sprint 3: Rendering, UI & Assets 
**Duration:** Week 3
**Focus:** Connect the GS logic to the actual visual PixiJS canvas. 
- [ ] Build basic Reel strip generator (using placeholder generic symbols).
- [ ] Attach `SlotEngine` states to visual PixiJS animations (Spinning blur -> Stop).
- [ ] Implement HTML/CSS UI overlay (Spin button, Balance Display, Bet Denomination Selector).
- [ ] Integrate Celebration logic: Generic particle effects/banner for Big/Huge/Mega Win thresholds.

## Sprint 4: Compliance, Overlays & Final Polish
**Duration:** Week 4
**Focus:** Implement the required regulatory overlays (Reality Check, Loss Limits, Autoplay, HUD) and aggregator logic.
- [ ] Build dynamic Paytable / Rules modal populated by config parameters.
- [ ] Implement Autoplay overlay (X spins, conditional stops).
- [ ] Build "Reality Check" timed interruption system.
- [ ] Implement Certification HUD (Session Time, Real Time Clock, Win/Loss Totals).
- [ ] Add `postMessage` listener for external aggregator commands (stop spins) and implement Home/Cashier redirects.
- [ ] Handle Free Round Bonus (FRB) visuals (Promotional Spin indicator).
- [ ] Final E2E GS Mock simulation demonstrating the Golden Flow.
- [ ] Perform UI/UX QA on mobile viewports.
- [ ] Create documentation and utility scripts (e.g., scaffolding CLI or Bash script) explaining how to use this template to generate new games.

