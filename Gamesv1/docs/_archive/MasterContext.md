> [!WARNING]
> This document is archived and may be outdated.
> Do not use it as source of truth.
> Canonical entrypoint: `docs/MasterContext.md` and `docs/DOCS_MAP.md`.
# PROJECT: GS Gamesv1 Universal Slot Template
# DOCUMENT: Master Context & Source of Truth

## 1. VISION
> To create a standardized, foolproof "cookie-cutter" template that allows any studio to build a slot game that perfectly integrates with the GS platform, including all math rules, network protocols, and frontend compliance requirements.

## 2. TECH STACK & ENVIRONMENT
* **OS:** Windows
* **Languages/Tools:** 
  * Frontend: HTML5, CSS, TypeScript, Vite (v4)
  * Graphics Engine: PixiJS
  * Networking: WebSockets (`abs.gs.v1` protocol)
  * Mock Server: Node.js (for local isolated testing without relying on the unfinished real GS)

## 3. STATUS
### ✅ Completed
* Project Folder Structure Created (`e:\Dev\GSRefactor\Gamesv1`)
* Initial Documentation (`PRD.md`, `NFR.md`, `SprintPlan.md`)
* Abstract WebSocket Client Implementation (The internal "messenger" that perfectly formats data for the GS)
* Basic Mock Server (A fake GS backend we use to test the game without needing the real, unfinished one)

### 🚧 In Progress (Current Focus)
* **Game Configuration Architecture:** Designing a central blueprint file (`master-game-config.json`) to hold RTPs, Bet Limits, and Max Exposure data.
* **Game Registration Rules:** Writing step-by-step instructions on how to tell the GS database that a new game exists and what its rules are.
* **Stand-alone Configuration Page:** Building a simple interface to tweak the game's math settings visually.

### 📅 Backlog
* Upgrading PixiJS implementation from v7 syntax to v8 syntax (fixing a small crash the visual robot found).
* Building the main visual UI (Spin Buttons, Balance HUD).
* Integrating generic spinning reels into the main screen.
* Reality Check & Compliance Overlays.

## 4. ARCHIVE / NOTES
* **Important Decision:** The end-user is a Project Manager/Owner, not a developer. All technical steps must be preceded by simple, analogy-based explanations.
* **Important Decision:** The GS (Game Server) backend is strictly "Not Ready". Focus heavily on providing configuration scaffolds, isolated mock testing, and DB registration guides so that when the GS *is* ready, games can plug right in.
* **Rule:** Strict idempotency is enforced. The client generates a unique `operationId` (a unique tracking stamp) for every single spin request to ensure the GS never accidentally double-charges a player.

