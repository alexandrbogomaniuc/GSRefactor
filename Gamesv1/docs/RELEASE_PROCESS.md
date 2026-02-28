# RELEASE_PROCESS

This document is the production release checklist for Gamesv1.
It is written for both release managers and AI agents.

## 0) Inputs Required Before Any Release
1. Target `gameId` (example: `premium-slot`).
2. Target environment (`staging` or `production`).
3. CDN base URL and version tag.
4. Operator launch URL templates for `guest`, `free`, and `real` modes.
5. DB change window / deployment window.

---

## A) What Is Stored In GS DB

At minimum, GS DB registration must store these fields for each game release:

1. `game_catalog` identity entry:
- `game_id`
- `game_name`
- `version`
- `status` (draft/active/disabled)

2. Client launch routing:
- `entrypoint_url` (CDN URL to built client)
- `asset_manifest_url` (usually `manifest.json` under the same CDN release)

3. Feature and compliance flags:
- autoplay/turboplay toggles
- min spin timing flags
- operator bridge requirements (if market/operator requires messaging)

4. RTP and math models:
- model IDs
- RTP values
- volatility

5. Financial and safety limits:
- min/max/default bet
- max exposure / cap multipliers
- currency or truncation constraints (when market requires integer cents)

### Source of Truth for DB Payload
- `games/<gameId>/game.settings.json`
- `games/<gameId>/gs/template-params.properties`
- `games/<gameId>/gs/template-params.json`
- `games/<gameId>/gs/registration.md`

Generate those artifacts with:
```bash
npm run config:gen
```

---

## B) End-to-End Release Steps (Numbered Checklist)

### 1. Build + AssetPack
1. Run type build for target game:
```bash
corepack pnpm --filter @games/<gameId> build
```
2. Run Vite build so AssetPack executes in build mode:
```bash
corepack pnpm --filter @games/<gameId> exec vite build
```
3. Confirm output artifacts:
- game bundle in `games/<gameId>/dist/`
- generated manifest in `games/<gameId>/src/manifest.json`

Release gate:
- Build exits with code 0.
- No missing asset aliases in manifest.

### 2. Upload CDN
1. Upload `dist/` and required static assets to versioned CDN path:
- Example: `/games/<gameId>/<version>/...`
2. Record final `entrypoint_url` and `asset_manifest_url`.

Release gate:
- CDN URLs are reachable over HTTPS.
- Cache headers/versioning policy are applied.

### 3. Generate Release Manifest
1. Create a release manifest artifact (JSON) including:
- `gameId`
- `version`
- `entrypointUrl`
- `assetManifestUrl`
- `buildTimestamp`
- `gitSha`
- `environment`
2. Store it with release artifacts (or in deployment metadata store).

Release gate:
- Manifest references exact deployed CDN version.

### 4. Generate SQL Registration Artifact
1. Generate GS template params first:
```bash
npm run config:gen
```
2. Produce SQL artifact (example path):
- `games/<gameId>/gs/registration.sql`
3. SQL artifact must include:
- game_catalog upsert (id/name/version/entrypoint)
- feature flags
- RTP models
- financial limits

Release gate:
- SQL reviewed by release manager.
- SQL matches current template-params outputs.

### 5. Deploy
1. Apply SQL registration change in target environment.
2. Deploy/activate CDN entrypoint URL in operator routing.
3. Roll out according to environment policy (staging first, then production).

Release gate:
- DB registration and CDN routing reference the same release version.

### 6. Verify Via Launch URLs (guest/free/real)
1. Test `guest` launch URL.
2. Test `free` launch URL.
3. Test `real` launch URL.
4. Verify:
- game boots and session starts
- spin round completes with balance updates
- localization/assets load correctly

Release gate:
- All three launch modes pass.
- No protocol errors in console/network logs.

---

## C) Runtime Handshake (Client Expectations)

### 1. Connect/Auth/Session Sync
1. Client opens transport (`WS` abs.gs.v1 or `EXTGAME`).
2. Client sends auth/init payload.
3. Client expects one of:
- `SESSION_ACCEPTED`
- `SESSION_SYNC`
4. Client records balance/session state from accepted/sync payload.

### 2. Round Financial Flow
1. Client sends `BET_REQUEST` with `operationId`.
2. Client waits for `BET_ACCEPTED` (or reject/error).
3. Client sends `SETTLE_REQUEST` with the same `operationId`.
4. Client waits for `SETTLE_ACCEPTED`.
5. Client updates balance from balance snapshot/update event.

### 3. Reconnect Recovery
1. On reconnect, client expects sync/restore state (`SESSION_SYNC` for WS or state echo flow for ExtGame).
2. Any retry of a monetary action must reuse the original `operationId`.
3. ExtGame mode must echo `gameState` and `lastAction` on transaction calls.

---

## Code Map (Step -> Implementation Files)

### Step 1: Build + AssetPack
- `packages/pixi-engine/src/engine.ts` (asset manifest init, bundle loading, base path resolution)
- `packages/ui-kit/src/assets.ts` (shared asset key surface)
- `packages/pixi-engine/src/resize/ResizePlugin.ts` (runtime canvas/device behavior after build)

Support tooling outside `packages/`:
- `games/<gameId>/scripts/assetpack-vite-plugin.ts`
- `games/<gameId>/vite.config.ts`

### Step 2: CDN Runtime Loading Expectations
- `packages/pixi-engine/src/engine.ts` (loads manifest bundles from deployed base path)
- `packages/i18n/src/index.ts` (loads locale JSON from deployed path)

### Step 3: Release Manifest Data Contracts
- `packages/core-protocol/src/schemas.ts` (transport envelope schema)
- `packages/core-compliance/src/config/RuntimeConfigSchema.ts` (runtime config fields/constraints)

### Step 4: SQL Registration Artifact Data Source
- `packages/core-compliance/src/FeatureFlags.ts` (flag model)
- `packages/core-compliance/src/ComplianceConfig.ts` (bank + flag compliance rules)
- `packages/core-compliance/src/config/RuntimeConfigSchema.ts` (limits, bets, exposure model)
- `packages/core-compliance/src/config/ConfigResolver.ts` (layer precedence for resolved runtime values)

Support tooling outside `packages/`:
- `tools/config-gen/src/index.ts` (generates `template-params` + `registration.md` artifacts)

### Step 5: Deploy / Operator Integration
- `packages/operator-pariplay/src/PariplayBridge.ts` (operator frame messaging contract)
- `packages/core-protocol/src/index.ts` (transport selection WS vs EXTGAME)

### Step 6: Launch Verification + Runtime Handshake
- `packages/core-protocol/src/IGameTransport.ts` (required connect/spin/settle interface)
- `packages/core-protocol/src/ws/GsWsTransport.ts` (WS handshake, bet/settle handling, balance events)
- `packages/core-protocol/src/http/ExtGameTransport.ts` (Enter/processTransaction flow, state echo)
- `packages/core-protocol/src/SpinProfiling.ts` (round telemetry payload support)

Verification support outside `packages/`:
- `tests/contract/transport.contract.test.ts`

---

## Final Sign-off Checklist (Release Manager + AI)
1. Build + AssetPack passed for target game.
2. CDN upload completed and URLs recorded.
3. Release manifest created and archived.
4. SQL registration artifact generated and reviewed.
5. Deployment completed in target environment.
6. Guest launch URL verified.
7. Free launch URL verified.
8. Real launch URL verified.
9. Handshake + settle flow validated in logs.
10. Reconnect/idempotency behavior validated.