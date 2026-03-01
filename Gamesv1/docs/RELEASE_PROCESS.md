# RELEASE_PROCESS

Production release checklist for Gamesv1.
This checklist is for release managers and AI agents.

## 0) Inputs Required Before Any Release

1. Target `gameId` (example: `premium-slot`).
2. Target environment (`staging` or `production`).
3. CDN base URL and version tag.
4. GS launch URL templates for `guest`, `free`, and `real` modes.
5. Deployment/change window.

## A) What Is Stored In GS DB

At minimum, GS registration data must include:

1. Catalog identity
- `game_id`
- `game_name`
- `version`
- `status`

2. Client routing
- `entrypoint_url`
- `asset_manifest_url`

3. Runtime/compliance profile
- feature flags
- min spin timing / turbo/autoplay policy
- limits and exposure

4. Math profile
- model IDs
- RTP values
- volatility

5. Financial constraints
- min/max/default bet
- currency/truncation settings

Source artifacts in repo:
- `games/<gameId>/game.settings.json`
- `games/<gameId>/gs/template-params.properties`
- `games/<gameId>/gs/template-params.json`
- `games/<gameId>/gs/registration.md`

Generate artifacts:
```bash
npm run config:gen
```

## B) End-to-End Release Steps

### 1. Build + AssetPack
1. Build target game:
```bash
corepack pnpm --filter @games/<gameId> build
```
2. Run production asset build:
```bash
corepack pnpm --filter @games/<gameId> exec vite build
```
3. Verify outputs:
- `games/<gameId>/dist/`
- `games/<gameId>/src/manifest.json`

Release gate:
- build exits 0
- manifest has no missing aliases

### 2. Upload CDN/static
1. Upload client bundle and static assets to versioned CDN path.
2. Record final `entrypoint_url` and `asset_manifest_url`.

Release gate:
- URLs reachable via HTTPS
- cache/version policy applied

### 3. Generate Release Manifest
Create release metadata JSON with:
- `gameId`
- `version`
- `entrypointUrl`
- `assetManifestUrl`
- `buildTimestamp`
- `gitSha`
- `environment`

Release gate:
- metadata references exact deployed version

### 4. Generate GS Registration Artifact
1. Generate template params:
```bash
npm run config:gen
```
2. Produce SQL/registration artifact.
3. Validate that DB registration fields align with release metadata.

Release gate:
- registration artifact reviewed
- artifact version == CDN version == manifest version

### 5. Deploy
1. Apply GS registration changes.
2. Activate CDN entrypoint for target environment.
3. Promote per environment policy.

Release gate:
- GS registration and CDN route same release version

### 6. Verify Launch URLs
1. Validate `guest` launch URL.
2. Validate `free` launch URL.
3. Validate `real` launch URL.
4. Verify:
- boot completes
- runtime session loads
- round request/response flow succeeds
- balance/session updates are reflected from GS responses

Release gate:
- all three modes pass
- no protocol/runtime errors in logs

## C) Runtime Handshake (Canonical HTTP Path)

### 1. Session Init
1. Client calls GS HTTP init/enter endpoint.
2. GS returns session, wallet snapshot, and runtime config.
3. Client stores response as read-only source state.

### 2. Round Transaction Flow
1. Client submits transaction request with idempotency key + sequencing data.
2. GS validates requestCounter/idempotency and resolves outcome.
3. GS returns updated wallet/session/result state.
4. Client renders result; client does not own wallet truth.

### 3. Restore/Recovery
1. On reload/reconnect, client calls init/enter again.
2. GS returns restore state if an interrupted round exists.
3. Client resumes presentation from GS-provided restore payload.

Boundary note:
- Browser transport remains `browser -> GS` only.
- Internal slot-engine/RNG/audit paths are private GS internals and not browser integration points.

## D) Legacy Note

- `abs.gs.v1` WebSocket transport is legacy/experimental only.
- Production target architecture is GS HTTP runtime path.

## Code Map (Step -> Implementation)

### Build + Asset Packaging
- `games/<gameId>/vite.config.ts`
- `games/<gameId>/scripts/assetpack-vite-plugin.ts`
- `packages/pixi-engine/src/engine.ts`

### Runtime Transport + Schemas
- `packages/core-protocol/src/http/ExtGameTransport.ts`
- `packages/core-protocol/src/IGameTransport.ts`
- `packages/core-protocol/src/schemas.ts`

### Runtime Config + Compliance
- `packages/core-compliance/src/config/RuntimeConfigSchema.ts`
- `packages/core-compliance/src/config/ConfigResolver.ts`
- `packages/core-compliance/src/animation/AnimationPolicy.ts`

### Registration Artifacts
- `tools/config-gen/src/index.ts`
- `games/<gameId>/gs/*`

### Verification
- `tests/contract/mock-gs/ScenarioRunner.ts`
- `tests/compliance/config.test.ts`
- `tests/compliance/animation-policy.test.ts`
- `tests/layout/layout-matrix.test.ts`

## Final Sign-off Checklist

1. Build + asset packaging passed.
2. CDN/static upload completed and version recorded.
3. Release metadata generated.
4. GS registration artifact generated and reviewed.
5. Deployment completed.
6. Guest/free/real launch checks passed.
7. HTTP runtime init + transaction + restore behavior validated.
8. Idempotency/request sequencing validated.
