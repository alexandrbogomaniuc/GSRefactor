# RELEASE_PROCESS

Production release process for Gamesv1 aligned to locked GS Phase-1 architecture.

## 0) Required inputs

1. `gameId`
2. target environment
3. release version
4. CDN/static origin
5. launch URL set (`guest`, `free`, `real`)

## 1) Canonical contracts

Before release execution, validate against:
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/bootstrap-config-contract.md`

## 2) Build and package

```bash
corepack pnpm --filter @games/<gameId> build
corepack pnpm run release:pack -- --game <gameId> --version <version> --static-origin <cdnBase>
```

Release-pack output must satisfy the artifact list in `docs/gs/release-registration-contract.md`.

## 3) Register in GS

1. Upload bundle/static assets to CDN.
2. Register `registration-artifact.json` and referenced manifests.
3. Confirm checksum validation from `checksums.sha256.json`.

## 4) Canary and promote

Execute `CANARY_CHECKLIST.md` and then promote only after successful checks.

## 5) Smoke verification

Execute `SMOKE_TEST_CHECKLIST.md` and verify at minimum:
- bootstrap
- opengame
- playround
- featureaction (if enabled)
- resumegame/restore
- gethistory
- requestCounter and idempotency duplicate behavior
- localization/content-path overrides

## 6) Rollback

If required, execute rollback from `ROLLBACK_PACK.md` and `docs/gs/enable-disable-canary-rollback.md`.

## Code map

- Runtime transport: `packages/core-protocol/src/http/GsHttpRuntimeTransport.ts`
- Runtime contract types: `packages/core-protocol/src/IGameTransport.ts`
- Config/capability resolver: `packages/core-compliance/src/ConfigResolver.ts`
- Runtime config schema: `packages/core-compliance/src/ResolvedRuntimeConfig.ts`
- Reference runtime client: `games/premium-slot/src/app/runtime/GsRuntimeClient.ts`
- Release pack generator: `tools/release-pack/create-release.ts`

## Deprecated note

Legacy `/v1/placebet` + `/v1/collect` browser contract is obsolete in canonical path.
Use slot-browser-v1 `/v1/playround` instead.
