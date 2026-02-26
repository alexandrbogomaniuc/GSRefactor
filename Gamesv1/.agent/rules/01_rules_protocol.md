# Non-Negotiable Rules for Slot Game Protocol Integration

This document defines the strict rule set for building, refactoring, and maintaining slot games within this ecosystem using the `abs.gs.v1` protocol and the `gs-protocol-mcp` context layer.

## 1. Operation Idempotency
Every monetary action (spin, buy feature, or gamble if server-charged) **MUST** include an `operationId` (UUID/GUID).
- **Retries:** If a network timeout or disconnect occurs, the client's retry payloads **MUST reuse the exact SAME `operationId`** associated with the original action. 
- **Consequence:** Failing to do so can result in double-charging the player's balance.

## 2. State Echo (Do Not Invent Endpoints)
The `abs.gs.v1` server is the absolute sole authority over game state.
- If the protocol provides a `gameState` during initialization (`Enter` message) or during recovery, the client must store it.
- When required by subsequent requests (e.g., passing a `lastAction` or `resumeState`), the client must echo back the exact state object previously provided. 
- **Rule:** Do NOT invent, assume, or manually construct state objects locally to send to the server.

## 3. Transport Abstraction
Game logic must never be tightly coupled to the underlying network transport (WebSocket vs. HTTP).
- Always provide and rely on an `IGameTransport` interface.
- Core game state machines should only listen to normalized events (e.g., `onSpinResult`, `onBalanceUpdated`) emitted by the abstraction layer, completely agnostic to whether the payload arrived via a WS frame or an HTTP fetch polyfill.

## 4. Compliance First
Regulatory and compliance features must be fully implemented in the baseline template *before* any game-specific theme/art content is developed:
- `turboplay` limits and toggles.
- Minimum spin time enforcement.
- Mandatory `postMessage` event dispatching (for operator outer-frames/iFrames).
- Spin profiling and RNG certification logging overlays.
- Bank properties and currency formatting constraints.

## 5. Performance Budget & Scene Growth
Slot games run infinitely. Uncontrolled scene graph growth will cause mobile browser tabs to crash due to out-of-memory errors.
- **Rule:** Never instantiate new PIXI objects endlessly without destroying or pooling them (e.g., particles, win lines, flying coins).
- Add dev-only instrumentation (like a native `DebugOverlay`) to detect increasing scene object counts and monitor texture memory/VRAM consumption.

## 6. No Secret Leakage
Never expose passwords, tokens, or security keys.
- **Rule:** Never commit `secretKeys`, `PATs`, or `operatorTokens` to the repository.
- Ensure the MCP redaction layer (`gs-protocol-mcp/redactor.ts`) and any client-side logging filters actively mask `[REDACTED]` over patterns resembling authorization strings.

---

## 🏁 Definition of Done (DoD) Checklist

Before a slot game client or core engine pull request can be considered "Done", it must pass all of the following validations:

- [ ] **Idempotency Validated**: Disconnecting the network precisely during a Spin request and reconnecting proves the retry payload reused the original `operationId`.
- [ ] **State Recovery Tested**: Reloading the browser mid-Free-Spins correctly parses the `Enter` message state and resumes from the exact exact spin index without requiring a new Bet.
- [ ] **No Magic Endpoints**: The client only communicates over defined `abs.gs.v1` channels and echoes state matching the server schemas (Verified via `gs-protocol-mcp` schema validations).
- [ ] **Compliance Verified**: The spin duration honors the configured `minSpinTime`, and the operator iFrame successfully receives `SessionReady` and `BalanceUpdate` `postMessage` payloads.
- [ ] **Zero Memory Leaks**: Running 500 automated spins does not steadily increase the PIXI render tree object count.
- [ ] **Secrets Audited**: A source code sweep confirms no hardcoded Bearer tokens, API keys, or developer signatures exist in checked-in files.
