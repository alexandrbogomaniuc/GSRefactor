# MasterContext

Canonical architecture context for Gamesv1.

If any document conflicts with this file and `docs/gs/*`, those sources win.

## Program Goal

Gamesv1 is the GS slot browser client shell and release-packaging environment for new slot games.

## Canonical Spec Split

- `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md` is the client capability and behavior spec.
- `docs/gs/*` is the runtime/release contract spec.

## Locked Runtime Target

1. Canonical runtime target is GS HTTP `slot-browser-v1`.
2. Browser talks only to GS public runtime endpoints.
3. Canonical browser endpoint prefix is `/slot/v1/*`.
4. Canonical history endpoint is `/slot/v1/gethistory`.
5. Browser is presentation-only for financial/session truth.
6. Internal slot-engine and RNG are private/internal server-side concerns.

## GS Authority Boundaries

GS is authoritative for:
- session lifecycle
- wallet/balance truth
- DB persistence
- requestCounter sequencing
- idempotency decisions
- unfinished-round restore and routing
- runtime config resolution

Browser must never own or fabricate authoritative wallet/session/DB truth.

## Runtime Operations Canon

- `bootstrap`
- `opengame`
- `playround`
- `featureaction`
- `resumegame`
- `closegame`
- `gethistory`

## Scope Constraints

- `abs.gs.v1` WebSocket is legacy/experimental only.
- Multiplayer is out of scope.
- Operator/Pariplay messaging is out of canonical runtime scope.

## Source Of Truth Map

- Architecture: `docs/MasterContext.md`, `docs/PROJECT.md`
- Client capability spec: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- Runtime/release contract spec: `docs/gs/README.md`, `docs/gs/*`
- Release execution: `docs/RELEASE_PROCESS.md`
- Index: `docs/DOCS_MAP.md`
