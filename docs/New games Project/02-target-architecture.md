# Target Architecture (GS-Core Strategy)

Last updated: 2026-02-11

## Architectural Principle
- Keep GS as the system of record and integration hub.
- Add a dedicated New Games runtime layer for new math/state and modern clients.
- Connect New Games runtime to GS through explicit internal contracts.

## Runtime Topology
1. Browser loads game via GS launch entry.
2. GS validates player/session/bank context and returns launch payload.
3. Browser loads new game client bundle (PixiJS latest).
4. Game client sends game commands to New Games Runtime API.
5. New Games Runtime asks GS to execute wallet-impacting operations.
6. GS calls casino side wallet endpoints and returns authoritative result.
7. New Games Runtime updates round state and emits events/history records.

## Responsibilities Split

### GS
- Auth/session lifecycle and lock semantics.
- Bank/subcasino/game configuration and launch policy.
- Wallet external integrations and pending operation safeguards.
- Existing support tools, error persistence, and operational controls.
- History/reporting persistence where already supported.

### New Games Runtime
- Game-specific command API (for Plinko first).
- Deterministic outcome orchestration and round state machine.
- Rate limiting, request counter checks, idempotency keys.
- Realtime updates stream to clients.
- Performance-focused gameplay loop independent from GS legacy client stack.

### New Client (PixiJS latest)
- Rendering, animation, UX, input handling.
- Localization and game-specific UI.
- Strict request sequencing (`requestCounter`) and resilience logic.

## Integration Contract Shape (Draft)
- `POST /ngs/opengame`
- `POST /ngs/placebet`
- `POST /ngs/collect`
- `POST /ngs/readhistory`

GS internal calls from NGS:
- `POST /gs-internal/session/validate`
- `POST /gs-internal/wallet/reserve-or-bet`
- `POST /gs-internal/wallet/credit-or-collect`
- `POST /gs-internal/history/write` (or reuse current GS persistence integration path)

## Non-Goals For Phase 1
- Replacing GS session, wallet, history, or support tooling.
- Global migration of legacy client games to modern frontend pipeline.
- Full compliance feature pack at kickoff.

