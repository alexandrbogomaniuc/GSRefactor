# Operator Integrations & Client Compliance

Before a slot game client is deemed production-ready, it must strictly adhere to the following checklist. Compliance features take priority over game-specific aesthetics.

---

## Technical & Regulatory Checklist

### [ ] 1. Bank Properties & Formats
- [ ] Display currency strings according to locale and `fractionDigits` settings provided by the `Enter` configuration (e.g. `1,234.56 USD` vs `1 234,56 €`).
- [ ] Validate max bet constraints dynamically.

### [ ] 2. `minSpinTime` Enforcement
- [ ] Ensure that spins artificially delay rendering loop evaluations if the spin time elapsed is lower than `minSpinTime` (often 2500ms or 3000ms in specific jurisdictions).
- [ ] Ensure that stopping reels early does not complete the cycle before `minSpinTime`.

### [ ] 3. Turboplay / Autoplay Hooks
- [ ] Implement toggleable turboplay that overrides visual timings but still honors server-sent `minSpinTime` metrics.
- [ ] Autoplay loops **must pause** on connection timeouts and wait indefinitely for a user-acknowledgement before resuming auto-spins.

### [ ] 4. Telemetry: `postMessage` Integration
- [ ] Operator iFrames / outer wrappers expect `postMessage` payloads.
- [ ] Fire `SessionReady` upon full client loading.
- [ ] Fire `BalanceUpdate` immediately after wins are locally settled.
- [ ] Escalate `SessionError` payloads if unrecoverable disconnects occur, triggering external modal UI.

### [ ] 5. Spin Profiling
- [ ] Measure exact time elapsed from emitting a Spin Request (via `operationId`) to the exact local execution of the results.
- [ ] Provide dev-mode overlays capturing FPS and performance allocations.
