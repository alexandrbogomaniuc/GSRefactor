# Non-Negotiable Protocol Rules

This rule file defines protocol and state-ownership requirements for Gamesv1.

## 1. Canonical Runtime Path

- Production target is GS HTTP runtime path.
- `abs.gs.v1` WebSocket is legacy/experimental only.

## 2. Ownership and Truth

- GS owns session, wallet, DB state, restore state, requestCounter, and idempotency decisions.
- Client is presentation-only for financial/state truth.
- Client must not invent authoritative state.

## 3. Idempotency + Sequencing

- Monetary requests must include stable idempotency key.
- Retries must reuse the same key.
- Client must follow GS requestCounter/ordering expectations.

## 4. Transport Abstraction

- Game logic must stay transport-agnostic via `IGameTransport`.
- No direct `WebSocket` calls from game modules.

## 5. Scope Constraints

- Operator-specific messaging (`postMessage`, Pariplay bridge) is out of canonical scope.
- Multiplayer is out of scope.

## 6. Definition of Done (Protocol)

- [ ] HTTP init + transaction + restore flow validated.
- [ ] Retry/idempotency behavior validated.
- [ ] Sequencing/requestCounter behavior validated.
- [ ] No client-owned wallet/DB state logic introduced.
