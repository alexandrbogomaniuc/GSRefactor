# MasterContext

Canonical architecture context for Gamesv1.
If any document conflicts with this file and `docs/gs/*`, those sources win.

## Program Goal

Gamesv1 is the best-in-class GS slot browser client shell and release-packaging environment.

## Locked Runtime Target

1. Canonical runtime transport is GS HTTP slot-browser-v1.
2. Browser talks only to GS public runtime endpoints.
3. Internal slot-engine host is private/internal only.
4. Browser is presentation-only for financial/session state truth.
5. Static assets are loaded from CDN/static origin.
6. Gamesv1 outputs versioned release artifacts for GS registration.

## GS Authority Boundaries

GS is authoritative for:
- session lifecycle
- wallet and balance truth
- DB persistence
- requestCounter sequencing
- idempotency decisions
- unfinished-round restore and routing
- runtime config resolution

Browser must never own or fabricate authoritative wallet/DB/session truth.

## Transport Canon

Canonical browser operations:
- `bootstrap`
- `opengame`
- `playround`
- `featureaction`
- `resumegame`
- `closegame`
- `gethistory`

Legacy `abs.gs.v1` WebSocket is legacy/experimental only.

## Package Responsibilities

- `packages/core-protocol`: canonical GS browser runtime transport and envelopes.
- `packages/core-compliance`: capability matrix, config layering, compliance policy.
- `packages/pixi-engine`: rendering bootstrap, asset loading, layout/runtime loop.
- `packages/ui-kit`: shared HUD/menus/dialog shell.
- `games/premium-slot`: reference game consuming canonical packages.

## Source-of-Truth Docs

- Architecture: `docs/MasterContext.md`, `docs/PROJECT.md`
- GS contracts: `docs/gs/bootstrap-config-contract.md`, `docs/gs/browser-runtime-api-contract.md`
- Error model: `docs/gs/browser-error-codes.md`
- Runtime sequences: `docs/gs/browser-runtime-sequence-diagrams.md`
- Release registration: `docs/gs/release-registration-contract.md`, `docs/gs/enable-disable-canary-rollback.md`

## Update Rule

1. Update `docs/gs/*` first when contract behavior changes.
2. Update this file and `docs/PROJECT.md` to keep architecture canon aligned.
3. Archive or deprecate conflicting docs under `docs/_archive/` or legacy protocol notes.
