# Protocol: ExtGame API

## Overview
The ExtGame API provides a secondary (often HTTP-based) integration standard depending on the operator matrix. While similar to the WebSocket flow, its state transitions rely heavily on RESTful patterns and specifically defined transaction endpoints.

## Core Flow and Endpoints

### 1. `Start` / `Enter`
- **Purpose**: Authenticates the session token and initializes the player's wallet.
- **Payload**: Contains token, currency, and operator configuration.
- **Returns**: The current `gameState` and active configurations. If an interrupted game exists, it returns the `lastAction` required for recovery.

### 2. `processTransaction`
- **Purpose**: The sole endpoint responsible for executing math model logic (spins, bonuses, gambles).
- **Idempotency**: Requests must provide a unique transaction reference (similar to `abs.gs.v1`'s `operationId`). Retries due to network failure must resubmit this exact reference.
- **Returns**: The evaluated win lines, grid state, and updated `gameState`.

### 3. `gameState` Object
- The `gameState` serves as the absolute source of truth. The client must never infer game progression.
- If the server explicitly requests state tracking to be echoed back on subsequent actions, the client must preserve this verbatim without manual modification.
