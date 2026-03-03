# RELEASE_PROCESS

Production release process for Gamesv1 aligned to GS slot-browser-v1 canon.

## Required inputs

1. `gameId`
2. target environment
3. release version
4. CDN/static origin
5. launch URL set (`guest`, `free`, `real`)

## Canonical contracts

Validate against:
- `docs/gs/README.md`
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/fixtures/*`

Spec split:
- `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md` = capability/behavior spec.
- `docs/gs/*` = runtime/release wire and operations contract spec.

## Build and package

```bash
corepack pnpm --filter @games/<gameId> build
corepack pnpm run release:pack -- --game <gameId> --version <version> --static-origin <cdnBase>
```

## Register in GS

1. Upload bundle/static assets to CDN.
2. Register `artifacts/release-registration.json` and referenced manifests.
3. Confirm checksum validation from `checksums.sha256.json`.

## Canary and promote

Execute `CANARY_CHECKLIST.md` then promote only after successful checks.

## Smoke verification

Execute `SMOKE_TEST_CHECKLIST.md` and verify at minimum:
- `/slot/v1/bootstrap`
- `/slot/v1/opengame`
- `/slot/v1/playround`
- `/slot/v1/featureaction` (if enabled)
- `/slot/v1/resumegame`
- `/slot/v1/gethistory`
- requestCounter monotonic behavior
- idempotency duplicate behavior
- restore and localization/content-path behavior

## Rollback

Execute rollback from `ROLLBACK_PACK.md` and `docs/gs/enable-disable-canary-rollback.md`.

## Deprecated note

`/v1/placebet`, `/v1/collect`, `/v1/readhistory`, and `/v1/*` canonical endpoint assumptions are obsolete.
