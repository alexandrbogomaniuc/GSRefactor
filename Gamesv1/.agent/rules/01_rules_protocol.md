# Non-Negotiable Protocol Rules

This rule file defines protocol and ownership requirements for Gamesv1.

## 1. Canonical runtime source

Use `docs/gs/browser-runtime-api-contract.md` and `docs/gs/bootstrap-config-contract.md`.

## 2. Canonical browser operations

- `bootstrap`
- `opengame`
- `playround`
- `featureaction`
- `resumegame`
- `closegame`
- `gethistory`

## 3. Ownership and truth

- GS owns session, wallet, DB state, restore state, requestCounter, idempotency, routing, and config resolution.
- Browser is presentation-only for financial/session truth.
- Browser never consumes internal slot-engine audit data as UI state truth.

## 4. Transport boundaries

- Canonical path is browser -> GS HTTP runtime only.
- No direct game-level `WebSocket` runtime path.
- Legacy `abs.gs.v1` is experimental only and not canonical.

## 5. Idempotency and sequencing

- Money-impacting calls must carry stable idempotency identity on retries.
- requestCounter must be monotonic and server-authoritative.
- currentStateVersion must be forwarded when provided.

## 6. Scope constraints

- Operator-specific messaging is outside canonical runtime path.
- Multiplayer is out of scope.

## 7. Definition of done

- [ ] transport methods align to slot-browser-v1 operation set
- [ ] premium-slot renders server presentation payloads only
- [ ] no game module owns wallet/DB state truth
- [ ] release artifacts match `docs/gs/release-registration-contract.md`
