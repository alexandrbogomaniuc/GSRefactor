> [!WARNING]
> This document is archived and may be outdated.
> Do not use it as source of truth.
> Canonical entrypoint: `docs/MasterContext.md` and `docs/DOCS_MAP.md`.
# Product Requirements Document (PRD): Gamesv1 Template

## 1. Objective
Create a unified, compliant, and highly reusable HTML5/PixiJS Slot Game Template. This template will act as the master baseline for all future 3rd party game integrations into the newly refactored Game Server (GS) platform (`abs.gs.v1` protocol).

## 2. Target Audience
- **Internal Game Studios:** To scaffold new games rapidly.
- **3rd Party Providers:** To integrate their math engines/assets while remaining compliant with GS orchestration and compliance rules.

## 3. Core Features & Capabilities

### 3.1 Initialization & Handshake
- **Parameter Ingestion:** The client must parse initialization parameters from the URL or a post-message iframe proxy (e.g., `sessionId`, `bankId`, `gameId`, `WSS URL`, `AuthToken`).
- **Connection Establishment:** The client must securely connect to the GS via WSS using the `abs.gs.v1` subprotocol.
- **Loading Screen:** A unified loading screen must display progress while awaiting `GAME_READY` from the GS and completing asset loading.

### 3.2 Game Orchestration & Idempotency
- **The Spin Lifecycle (Wager -> Settle):**
  - Player presses spin.
  - Client generates a unique UUID `operationId`.
  - Client sends `BET_REQUEST` to GS.
  - Client must lock the UI and wait for `BET_ACCEPTED` (or `REJECTED`) payload detailing the deterministic outcome.
  - Reel simulation occurs visually based on the received outcome.
  - Client sends `SETTLE_REQUEST` using the identical `operationId`.
  - Client waits for `SETTLE_ACCEPTED`.
  - UI unlocks and balance is updated via `BALANCE_SNAPSHOT`.

### 3.3 Compliance & Regulatory Elements
- **Reality Check:** 
  - Periodic modal interruptions based on GS configuration (`rcInterval`).
  - Must pause gameplay completely and offer "Continue" or "Exit".
- **Certification HUD (Always visible or toggleable):**
  - Current Date and Time (Local to player).
  - Game Session Duration.
  - Total Bet and Total Win accumulated per session.
- **Loss Limits / Session Limits:** Visible counters displaying maximum session duration and session loss limits.
- **Rules & Paytable:** Dynamic rendering of rules derived from the GS game config (e.g., RTP, Max Win).

### 3.4 Player Experience Overlays
- **Autoplay Engine:** Ability to select X spins with conditions (stop on feature, stop on balance decrease).
- **Turboplay:** Toggle to visually accelerate reel simulation without bypassing the strict `BET -> SETTLE` network handshake.
- **Celebration Choreography:** Standardized overlay hooks for Big/Huge/Mega wins based on multiplier thresholds.
- **Free Round Bonus (FRB):** Dedicated visual state indicating the player is spinning using promotional funds/spins provided by the GS.

### 3.5 Interoperability & External Navigation
- **Aggregator / Wrappers:** The client must listen for `postMessage` events from the parent window (e.g., Pariplay events) to programmatically stop gameplay or halt Autoplay.
- **Navigation Redirects:** The game must be able to redirect the user to a `homeUrl` or `cashierUrl` (configured via GS) without breaking the session state.

### 3.6 Localization & Formatting
- Automatic application of language dictionaries based on GS initialization payload.
- Fallback to generic English (`en`) if a locale string is missing.
- **Financial Formatting:** Payouts must be displayed using the real currency symbol configured by the GS, and fractional monetary parameters must be truncated strictly as required by the bank policy.

## 4. User Journeys
1. **Happy Path:** Player loads -> Auto-connects -> Spins manually -> Wins -> Balance updates.
2. **Disconnection Recovery:** Player spins -> Network drops before `BET_ACCEPTED` -> Player reloads -> Client sends `RECONNECT_REQUEST` with `lastAckSeq` -> GS sends `SESSION_SYNC` resolving the pending spin -> Client animates the recovered result.

