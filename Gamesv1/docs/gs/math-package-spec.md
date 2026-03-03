# GS Math Package Specification

- Status: Draft for implementation
- Date: 2026-02-28
- Owners: GS Platform + Slot Engine Runtime
- Related:
  - `docs/adr/0001-new-slots-target-architecture.md`
  - `docs/gs-release-runtime-integration-flow.md`

## 1) Purpose

Define the immutable, versioned package format for per-game slot math used by the internal slot-engine host while keeping GS authoritative for session, wallet, persistence, routing, and unfinished-round restore.

## 2) Non-negotiable Policy

1. Production math is immutable per released `mathPackageVersion`.
2. Production math is loaded only at deploy/register time or engine startup.
3. Production math is never injected, uploaded, or swapped per-launch.
4. Browser never supplies math logic or executable math payloads.
5. Slot-engine host never writes directly to GS databases.

## 2.1 Ownership Boundary (Normative)

- GS owns math package/version and RTP model selection at config resolution time.
- Slot-engine host owns package loading/execution only for pre-registered, verified versions selected by GS.
- Slot-engine host must reject any attempt to execute unregistered, unsigned, or browser-supplied version/model input.

## 3) Package Artifact Model

Each math package is a signed immutable artifact stored in Artifact Registry and referenced by GS release manifest.

### 3.1 Artifact Layout

```text
math-package-{gameId}-{mathPackageVersion}.tar.zst
  /manifest.json
  /math.bin                      # compiled deterministic math payload
  /rtp-models/{modelId}.json     # metadata only, no runtime mutability
  /schemas/state-v{n}.json       # state serialization schema
  /schemas/outcome-v{n}.json     # outcome serialization schema
  /checksums/sha256sums.txt
  /SIGNATURE.ed25519
```

### 3.2 Required Identity Fields

`manifest.json` MUST include:

- `gameId` (string)
- `mathPackageVersion` (string, semver)
- `artifactHash` (string, SHA-256 of canonical archive)
- `artifactSignature` (string or detached signature reference)
- `serializationFormat` (for example `json-schema-v1`, `protobuf-v3`)
- `engineContractVersion` (for example `slot-runtime-v1`)

## 4) Manifest Schema (Normative)

```json
{
  "gameId": "10045",
  "mathPackageVersion": "1.4.2",
  "artifactHash": "sha256:7c9e3b...",
  "artifactSignature": "ed25519:ab12...",
  "engineContractVersion": "slot-runtime-v1",
  "serializationFormat": "json-schema-v1",
  "supportedFeatureFlags": [
    "FREE_SPINS",
    "RESPIN",
    "HOLD_AND_WIN",
    "BUY_FEATURE",
    "JACKPOT_HOOKS"
  ],
  "rtpModels": [
    {
      "modelId": "base-96.20",
      "rtpPercent": 96.2,
      "jurisdictionTags": ["ROW"],
      "defaultForBanks": false
    },
    {
      "modelId": "regulated-94.00",
      "rtpPercent": 94.0,
      "jurisdictionTags": ["REG_X"],
      "defaultForBanks": true
    }
  ],
  "maxWinCap": {
    "type": "BET_MULTIPLIER",
    "value": 10000,
    "hardStop": true
  },
  "stateSchemaVersion": 3,
  "outcomeSchemaVersion": 2,
  "compatibility": {
    "minGsRuntimeVersion": "gs-2026.02",
    "maxGsRuntimeVersion": "gs-2027.01",
    "minSlotEngineVersion": "engine-1.9.0",
    "backwardCompatibleStateFrom": [2, 3],
    "forwardCompatibleStateTo": [3]
  },
  "build": {
    "gitCommit": "abcdef1234",
    "builtAtUtc": "2026-02-28T19:00:00Z",
    "builder": "ci-release"
  }
}
```

## 5) Compatibility Rules

### 5.1 Runtime Compatibility

- GS MUST reject registration if:
  - `engineContractVersion` is unsupported,
  - signature/hash validation fails,
  - required features are missing for the game profile,
  - runtime version gates fail.
- Slot-engine host MUST refuse to load artifact when compatibility checks fail.

### 5.2 State Compatibility

- `stateSchemaVersion` controls persisted unfinished-round replay compatibility.
- Backward compatibility is required for at least previous `N-1` schema when rolling upgrades are active.
- Breaking state schema changes require:
  - explicit migration plan,
  - canary and replay validation,
  - controlled rollout gate in GS config.

### 5.3 Feature Compatibility

- `supportedFeatureFlags` is declarative and immutable for package version.
- GS capability resolver MUST not enable a runtime feature absent in package flags.

## 6) Load Lifecycle (Normative)

### 6.1 Allowed Load Points

1. Deploy/register time:
   - CI publishes artifact.
   - GS release manifest links `gameId -> mathPackageVersion`.
2. Slot-engine host startup/warmup:
   - Host preloads package(s) referenced by active GS manifests.

### 6.2 Forbidden Load Points

- Any production per-launch math load, upload, or override.
- Any launch query parameter or client payload that changes math version/model.

## 7) Selection and Resolution

- GS resolves `gameId`, bank policy, and approved RTP model identifier.
- GS passes resolved immutable references to slot-engine host:
  - `mathPackageVersion`
  - `rtpModelId`
- Slot-engine host executes only against preloaded, verified package selected by GS.

## 7.1 Deterministic Restore Constraint

- Persisted GS unfinished-round snapshot must include sufficient opaque engine state for deterministic reconstruction.
- Continuation must never depend on hidden in-memory RNG state only.
- If deterministic restoration cannot be reproduced from persisted snapshot + selected package/version/model, round continuation must be blocked.

## 8) Audit and Change Management

Every package change MUST produce:

1. New `mathPackageVersion` (no overwrite).
2. New artifact hash/signature.
3. Release note with:
   - feature flags changed,
   - RTP metadata changes,
   - max-win cap changes,
   - schema compatibility impact.
4. Replay evidence on representative unfinished-round samples.
5. Approval trace for certification/compliance where required.

## 8.1 Replay and Golden-Test Corpus Requirements

Each new or changed math package MUST include corpus artifacts in CI evidence:

1. Sample snapshots:
   - serialized unfinished-round snapshots covering base game and each supported feature mode.
2. Replay vectors:
   - deterministic inputs (including selected model and persisted opaque state references) used for replay checks.
3. Expected deterministic outputs in test mode:
   - outcome payloads and outcome hash values expected for each replay vector.
4. Rolling-upgrade schema compatibility evidence:
   - proof that `N-1` snapshots replay correctly on `N` package when declared compatible,
   - proof that declared incompatible schemas fail with explicit compatibility error.

## 9) Error Handling

Recommended GS errors:

- `MATH_PACKAGE_NOT_REGISTERED` (404)
- `MATH_PACKAGE_INCOMPATIBLE` (409)
- `MATH_PACKAGE_SIGNATURE_INVALID` (409)
- `MATH_PACKAGE_LOAD_FAILED` (502)

For these failures, GS must fail safely and keep financial state unchanged.

## 10) Anti-Patterns (Do Not Do)

- Reusing a version tag for changed bytes.
- Allowing `mathPackageVersion` from browser request data.
- Hot-patching math in running production node without new registered version.
- Using unsigned artifacts.
- Embedding environment-specific secrets in package manifest.
