# GS Release Registration Contract for New Slots

- Status: Draft for implementation (Phase 1)
- Date: 2026-03-01
- Contract version: `slot-release-registration-v1`
- Scope: Gamesv1 release artifacts -> GS Ops registration -> GS runtime selection
- Related:
  - `docs/adr/0001-new-slots-target-architecture.md`
  - `docs/gs/math-package-spec.md`
  - `docs/gs/bootstrap-config-contract.md`
  - `docs/gs/browser-runtime-api-contract.md`

## 1) Purpose

Define the exact data GS must register/store per slot release so Gamesv1 can produce release-pack outputs that map 1:1 with GS onboarding and rollout controls.

## 2) Non-Negotiable Rules

1. GS selects route, client package version, math package version, and RTP model.
2. No launch-time math injection in production.
3. Browser assets are served from CDN/static origin and pinned by version/hash.
4. Internal slot-engine host loads/executes only GS-selected, pre-registered verified versions.

## 3) Canonical Registration Object (Normative)

One registration record per `(gameId, releaseId)`.

```json
{
  "contractVersion": "slot-release-registration-v1",
  "releaseId": "slot10045-r2026.03.01-01",
  "gameId": "10045",
  "title": "Example Slot Title",
  "route": {
    "routeGameId": "10045",
    "routingEnabled": true,
    "legacyFallbackEnabled": true
  },
  "client": {
    "clientPackageVersion": "2.6.0",
    "assetBaseUrl": "https://cdn.example.com/slots/10045/2.6.0/",
    "assetBundleHash": "sha256:84f0...",
    "assetManifestPath": "/manifest.json"
  },
  "math": {
    "mathPackageVersion": "1.4.2",
    "allowedRtpModels": [
      {
        "rtpModelId": "base-96.20",
        "rtpPercent": 96.2,
        "jurisdictionTags": ["ROW"]
      },
      {
        "rtpModelId": "regulated-94.00",
        "rtpPercent": 94.0,
        "jurisdictionTags": ["REG_X"]
      }
    ],
    "defaultRtpModelRules": {
      "ruleType": "BANK_POLICY_THEN_RELEASE_DEFAULT",
      "releaseDefaultRtpModelId": "base-96.20",
      "fallbackOrder": ["regulated-94.00"]
    }
  },
  "capabilityProfile": {
    "profileRef": "cap-slot10045-v5",
    "profileHash": "sha256:af91...",
    "policySchemaVersion": "slot-bootstrap-v1"
  },
  "rollout": {
    "state": "DISABLED",
    "canary": {
      "mode": "ALLOWLIST",
      "banks": [6274],
      "subCasinoIds": [507],
      "trafficPercent": 5
    },
    "rollbackTargetReleaseId": "slot10045-r2026.02.20-03"
  },
  "compatibility": {
    "minGsVersion": "gs-2026.03",
    "maxGsVersion": "gs-2027.01",
    "browserContractVersion": "slot-browser-v1",
    "bootstrapContractVersion": "slot-bootstrap-v1",
    "internalRuntimeContractVersion": "slot-runtime-v1"
  },
  "integrity": {
    "registrationHash": "sha256:19cd...",
    "signature": "ed25519:ab12...",
    "signer": "gamesv1-release-bot",
    "generatedAtUtc": "2026-03-01T10:00:00Z"
  },
  "metadata": {
    "changeTicket": "REL-4821",
    "notes": "Phase-1 rollout",
    "createdBy": "gamesv1"
  }
}
```

## 4) Required Fields by Requirement Group

## 4.1 Identity

- `gameId` (required)
- `releaseId` (required, globally unique)

## 4.2 Client Artifact

- `client.clientPackageVersion` (required)
- `client.assetBaseUrl` (required)
- `client.assetBundleHash` (required)

## 4.3 Math and RTP

- `math.mathPackageVersion` (required)
- `math.allowedRtpModels[]` (required, non-empty)
- `math.defaultRtpModelRules` (required)

## 4.4 Capability and Rollout

- `capabilityProfile.profileRef` + `profileHash` (required)
- `rollout.state` in `DISABLED | CANARY | ENABLED | ROLLBACK_TARGET` (required)
- `rollout.rollbackTargetReleaseId` (required when state is `ROLLBACK_TARGET`)

## 4.5 Compatibility and Integrity

- `compatibility.*` version gates (required)
- `integrity.registrationHash`, `signature`, `signer` (required)

## 5) GS Storage Model (Normative)

GS must persist registration data in durable config storage and cache with these logical indexes:

1. `release_by_game_release_id[(gameId, releaseId)] -> registration object`
2. `active_release_by_game[gameId] -> releaseId`
3. `rollout_state_by_game[gameId] -> rollout state + canary config`
4. `rollback_target_by_game[gameId] -> releaseId`

GS must also retain previous releases for rollback and audit.

## 6) RTP Default Selection Rules

For each session launch:

1. Resolve bank/jurisdiction constraints.
2. If bank policy explicitly selects model and model is in `allowedRtpModels`, use it.
3. Else use `defaultRtpModelRules.releaseDefaultRtpModelId` if allowed for jurisdiction.
4. Else use first compatible model from `fallbackOrder`.
5. If none match, fail launch (`RTP_MODEL_NOT_AVAILABLE`).

## 7) Immutable Values for an Active Session

The following are immutable for session lifetime once `sessionId` is created:

1. `clientPackageVersion`
2. `mathPackageVersion`
3. selected `rtpModelId`
4. `capabilityProfile.profileHash` (or equivalent GS config hash)

New release activation must not mutate these values for already-active sessions.

## 8) Restore Persistence Requirements

To support deterministic unfinished-round restore, GS must persist:

## 8.1 Session State Core

- `sessionId`
- `gameId`
- `releaseId`
- `requestCounter`
- `stateVersion`
- current wallet/balance-visible state (authoritative)

## 8.2 Deterministic Reconstruction References

- `clientPackageVersion`
- `mathPackageVersion`
- selected `rtpModelId`
- `capabilityProfileHash`
- `restoreSnapshotBlob` (opaque engine state)

## 8.3 Optional Audit Correlators

- last `outcomeHash`
- last `engineBuildVersion`
- last `rngAlgorithmVersion` (server-side audit only)

Continuation must not rely on hidden in-memory state only.

## 9) Registration Validation Rules

GS must reject registration if any of the following fails:

1. Missing required field.
2. Duplicate `releaseId` with different content hash.
3. Unknown or incompatible contract version.
4. Invalid signature/hash.
5. `allowedRtpModels` empty or default rule references unknown model.
6. Client or math artifact integrity mismatch.

## 10) Gamesv1 Release-Pack Output Contract

Gamesv1 must produce:

1. `release-registration.json` (exact schema above)
2. `client-artifact-manifest.json` (bundle file list + hashes)
3. `math-package-manifest.json` (or pointer to registered math package manifest)
4. `release-evidence.md` (smoke/canary readiness evidence)
5. detached signature file (if not embedded in JSON)

GS Ops should not hand-edit these outputs.

## 11) Lifecycle State Meanings

- `DISABLED`: registered, not serving traffic.
- `CANARY`: enabled only for configured canary scope.
- `ENABLED`: default active release for production scope.
- `ROLLBACK_TARGET`: registered target selected for rollback activation.

