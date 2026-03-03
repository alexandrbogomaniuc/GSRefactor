# GS Contract Pack (Canonical, Locked)

This directory is the only canonical runtime/release contract source for Gamesv1.

If any other document conflicts with files in this folder, this folder wins.

## Canonical runtime target

- Browser -> GS HTTP only (`slot-browser-v1`)
- Endpoint prefix: `/slot/v1/*`
- Canonical history endpoint: `/slot/v1/gethistory`
- Browser is presentation-only for financial/session truth

## Contract files

- `bootstrap-config-contract.md`
- `browser-runtime-api-contract.md`
- `browser-error-codes.md`
- `browser-runtime-sequence-diagrams.md`
- `release-registration-contract.md`
- `enable-disable-canary-rollback.md`
- `fixtures/*`
- `schemas/*`
- `contract-lock.json`

## Spec split

- Client capability/behavior spec: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- Runtime/release contract spec: `docs/gs/*`

## Locking

Use `tools/verify-gs-contract-pack.ts` to verify `docs/gs/*` against `docs/gs/contract-lock.json`.
