# PROJECT: 3rd Party Slot Game Integration Template
# DOCUMENT: Master Context & Source of Truth

## 1. VISION
> A reusable, compliance-ready HTML5 / PixiJS template for 3rd party slot games that automatically implements the strict `abs.gs.v1` WebSocket protocol (supporting financial idempotency) and premium "wow" factor animations. Organized as a scalable monorepo.

## 2. TECH STACK & ENVIRONMENT
* **OS:** Windows / PowerShell
* **Languages/Tools:** TypeScript, Pixi.js v8, Node, Vite, pnpm (Workspaces).
* **Network Protocol:** `abs.gs.v1` over secure WebSocket (Canonical Message Envelopes).

## 3. STATUS
### ✅ Completed
* **Monorepo Restructure**: Reorganized project into `/packages`, `/games`, and `/tools`.
* **Canonical Template**: Refactored `/games/template-slot` to depend on shared packages.
* **Shared Logic**: Extracted core rendering and UI logic into `@gs/slot-shell` package.
* **Node Scaffolder**: Implemented `/tools/create-game` CLI tool for standardized game creation.
* Implemented `GSWebSocketClient.ts` with auto-reconnect and strict schema validation.
* Built local engine state machine in `SlotEngine.ts`.
* Frontend: BGaming-inspired responsive layout, Paytable modals, Certification HUD.
* Setup Documentation: Created `docs/TemplateIntegrationGuide.md`.
* [games/template-slot] Completed `/perf_audit` (Masking optimization, Symbol pooling, GPU redraws).
* **Environment**: Fixed Reel type errors by installing dependencies and configuring monorepo `tsconfig.json`.

### 🚧 In Progress (Current Focus)
* [games/template-slot] Refining the "Feature Module" system for pluggable game mechanics.
* Finalizing the dependency link between games and `@gs/slot-shell`.

### 📅 Backlog
* **Spine Integration**: Hook up `@esotericsoftware/spine-pixi-v8` for major symbols.
* **AssetPack + Compressed Textures**: KTX2 / Basis atlas compression.
* **Effekseer VFX**: Evaluate for advanced 3D win lines. 

## 4. ARCHIVE / NOTES
* **Scaffolding**: Run `pnpm create-game --name Name --id ID --slug slug` from root.
* **Structure**: Use `/packages` for core logic, `/games` for implementations, `/tools` for mocks.
* **Idempotency**: Bet and Settle requests MUST use the exact same locally generated UUID `operationId`.
* **Agent Rule:** All changes sent to the agent need to be placed in folder `e:\Dev\GSRefactor\Gamesv1`.
