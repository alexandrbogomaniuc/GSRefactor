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
* **Config System**: Unified layered configuration (Design-time + Runtime) via `@gs/config`.
* **Config Generator**: Automated GS registration output via `/tools/config-gen`.
* **Localization**: Implemented `@gs/i18n` with namespace support and MISSING_KEY validation tool.
* **Documentation Audit**: Archived legacy docs and unified the Master Context in `.agent/`.
* **Environment**: Fixed Reel type errors and configured IDE path aliases for `@gs/*` workspace packages.
* **Repo Cleanup**: Normalized canonical client template to `premium-slot-client`, archived legacy tools, and unified root `README.md`.

### 🚧 In Progress (Current Focus)
* [games/premium-slot-client] Refining the "Feature Module" system for pluggable game mechanics.
* Finalizing AssetPack pipelines for high-performance mobile texture compression.

### 📅 Backlog
* **Spine Integration**: Hook up `@esotericsoftware/spine-pixi-v8` for animated game characters.
* **Effekseer VFX**: Evaluate for advanced 3D win lines and screen-space effects.
* **Contract Testing Extension**: Add more complex jackpot/bonus scenarios to the ScenarioRunner.

## 4. ARCHIVE / NOTES
* **Scaffolding**: Run `npm run create-game -- --name Name --id ID --slug slug` from root.
* **Config Gen**: Run `npm run config:gen` to update GS registration files.
* **i18n Check**: Run `npm run i18n:check` to ensure translation completeness.
* **Idempotency**: Bet and Settle requests MUST use the same `operationId` UUID.
* **Agent Rule:** All changes sent to the agent need to be placed in folder `e:\Dev\GSRefactor\Gamesv1`.

