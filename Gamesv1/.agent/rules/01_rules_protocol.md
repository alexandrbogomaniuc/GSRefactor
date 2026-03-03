# Non-Negotiable Protocol Rules

This rule file defines runtime ownership and transport requirements for Gamesv1.

## Canonical sources

- Runtime/release contract spec: `docs/gs/*` (entry: `docs/gs/README.md`)
- Client capability/behavior spec: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`

## Canonical runtime target

- Browser -> GS HTTP only (`slot-browser-v1`).
- Canonical endpoint prefix: `/slot/v1/*`.
- Canonical history endpoint: `/slot/v1/gethistory`.

## Canonical browser operations

- `bootstrap`
- `opengame`
- `playround`
- `featureaction`
- `resumegame`
- `closegame`
- `gethistory`

## Ownership and truth

- GS owns session, wallet, DB state, restore state, requestCounter, idempotency, routing, and config resolution.
- Browser is presentation-only for financial/session truth.
- Browser must not consume internal slot-engine audit data as UI state truth.

## Scope constraints

- `abs.gs.v1` WebSocket is legacy/experimental only.
- Operator/Pariplay messaging is out of canonical runtime scope.
- Multiplayer is out of scope.

## Implementation constraints

- No canonical game runtime path may rely on direct `WebSocket`.
- No canonical game runtime path may rely on operator messaging bridge APIs.
- requestCounter must be monotonic and server-authoritative.
- idempotency identities must be stable across retries.
