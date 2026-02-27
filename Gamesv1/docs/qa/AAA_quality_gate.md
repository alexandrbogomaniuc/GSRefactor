# 🛡️ AAA Quality Gate - Release Checklist

This document defines the **Non-Negotiable** quality gates required for any Slot Game release within the GS Refactor framework. No build shall be promoted to Production without a 100% pass rate on these items.

## 1. Protocol Correctness
- [ ] **Operation Idempotency**: Verified that `operationId` is unique per monetary action and retries with the SAME ID return a cached server result without double-charging.
- [ ] **Reconnect Behavior**: Verified that the game recovers from a WebSocket drop mid-spin and uses `SYNC_REQUEST` (or equivalent) to restore the UI state.
- [ ] **State Echo (ExtGame)**: Verified that `lastAction` and `gameState` are echoed back exactly as received from the server.
- [ ] **Spin Profiling**: Verified that `PRECSPINSTAT` (including `SPINREQTIME` and `SPINANMTIME`) is correctly measured and attached to the *next* spin request.

## 2. Compliance & Regulatory
- [ ] **Turbo Play Rules**: Verified that "Turbo" mode only shortens/skips animations and never speeds up or alters the server-driven outcome.
- [ ] **Min Spin Time**: Verified that a minimum of ~1.5 - 3.0 seconds (as per jurisdiction) is enforced between spin start and result presentation, even if server data arrives early.
- [ ] **PostMessage Ordering**: Verified that operator events follow the sequence: `ticketReceived` -> `roundEnded`.
- [ ] **Autoplay Stop**: Verified that `stopAutoBet_pp` immediately enqueues an autoplay exit and stops as soon as the current spin cycle finishes.

## 3. Performance & Stability
- [ ] **Memory Integrity**: Verified no uncontrolled **Scene Graph** growth (leaked Sprite/Container instances) over a 2,000 spin soak test.
- [ ] **GPU Buffer Stability**: Verified that Texture Memory remains stable and does not climb monotonically.
- [ ] **Target Framerate**: Maintained steady **60 FPS** on mid-tier mobile devices (iPhone 13 / Pixel 6).
- [ ] **Low-Perf Fallback**: Verified that the game automatically degrades effects (particles, filters) if the frame budget is exceeded.

## 4. Security
- [ ] **Secret Leakage**: Verified that no API keys, debug tokens, or internal URLs are printed to the production console.
- [ ] **Log Sanitization**: Verified that the `redactor` middleware is active in all `chrome-devtools` or remote logging outputs.
