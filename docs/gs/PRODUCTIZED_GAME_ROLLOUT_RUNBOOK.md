# GS Productized Game Rollout Runbook

- Status: Operational guidance (Preparation GREEN -> Productization)
- Scope: GS-side rollout readiness and controlled promotion for new slot releases
- Non-goals: No architecture changes, no API/contract changes
- Canonical references:
  - `docs/gs/release-registration-contract.md`
  - `docs/gs/enable-disable-canary-rollback.md`
  - `docs/gs/browser-runtime-api-contract.md`
  - `docs/gs/bootstrap-config-contract.md`
  - `docs/gs/internal-slot-runtime-contract.md`

## 1) Purpose

Provide one repeatable GS operations flow for productized slot rollout covering registration, canary, production enablement, rollback, and validation gates required before scaling to multiple games.

## 2) Readiness Preconditions

1. Release pack is delivered by Gamesv1 with integrity artifacts.
2. Release registration payload validates against `slot-release-registration-v1`.
3. GS environment and on-call roster are confirmed for rollout window.
4. Rollback target release is already present and valid.
5. Monitoring dashboard and alert routing are active.

## 3) Release Registration Run

## 3.1 Intake

Collect and archive:
1. `release-registration.json`
2. `client-artifact-manifest.json`
3. `math-package-manifest.json`
4. evidence bundle from Gamesv1 smoke

## 3.2 Register

1. Create/update GS release registration record for `(gameId, releaseId)`.
2. Keep rollout state `DISABLED`.
3. Confirm these fields are persisted exactly:
   - `clientPackageVersion`
   - `assetBaseUrl`, `assetBundleHash`
   - `mathPackageVersion`
   - allowed/default RTP model set
   - capability profile/hash

## 3.3 Verify Registration Integrity

1. Verify release signature/hash fields.
2. Verify artifact hashes match manifests.
3. Verify compatibility versions are accepted by current GS runtime.
4. Record registration evidence (timestamp, operator, result, checksum set).

## 4) Canary Enablement Run

## 4.1 Canary Pre-Enable

1. Set explicit canary scope (allowlist banks/sub-casino and/or low traffic %).
2. Confirm release state transition to `CANARY` only.
3. Confirm previous release remains available as rollback target.

## 4.2 Canary Validation Matrix

Execute and capture evidence for:
1. Launch/bootstrap.
2. `openGame`, `playRound`, `featureAction`, `closeGame`.
3. Duplicate/idempotency behavior on mutating endpoints.
4. History retrieval (`getHistory`) correctness.
5. Session reconnect + unfinished-round restore.

## 4.3 Canary Health Gates

Minimum gates to promote:
1. Launch success within expected SLO.
2. Wallet reserve/settle flow stable (no unexplained financial drift).
3. Restore success rate acceptable (no deterministic replay mismatches).
4. Error profile has no critical pattern growth.
5. No config/version mismatch anomalies for pinned sessions.

If any critical gate fails: stop promotion and execute rollback plan.

## 5) Production Enablement Run

1. Move release from `CANARY` to `ENABLED` after gate sign-off.
2. Confirm active session pinning behavior:
   - `clientPackageVersion` pinned per session
   - `mathPackageVersion` pinned per session
   - RTP model pinned per session
   - capability/config hash pinned per session
3. Confirm new sessions resolve to newly enabled release.
4. Confirm old active sessions continue safely on originally pinned versions.

## 6) Rollback Run

## 6.1 Trigger Conditions

Trigger rollback immediately on:
1. Financial integrity issue (reserve/settle inconsistency).
2. Restore determinism failure.
3. Major launch/runtime failure trend during canary or production.

## 6.2 Rollback Steps

1. Set faulty release non-active (`DISABLED` or equivalent per runbook state model).
2. Set and activate rollback target release.
3. Refresh routing/config caches.
4. Re-run short smoke on rollback target.
5. Confirm new launches land on rollback target.
6. Confirm existing sessions remain stable without forced mid-session mutation.

## 6.3 Rollback Exit Criteria

1. Rollback target healthy under smoke and monitoring window.
2. Error/financial metrics return to baseline corridor.
3. Incident log and evidence package completed.

## 7) Session Restore Validation Guidance

For each rollout, run and record:
1. Mid-round disconnect scenario.
2. Reconnect and `resumeGame` with persisted snapshot.
3. Deterministic continuation outcome check.
4. `requestCounter` and `stateVersion` continuity check.
5. Close/settle consistency after resumed play.

Pass condition: resumed flow matches expected deterministic path with no hidden-state dependency failures.

## 8) History Validation Guidance

Validate history for:
1. Presence of latest completed round entries.
2. Correct round ordering and pagination behavior.
3. Correct bet/win/net values shown to client.
4. Feature rounds correctly represented in history details.
5. No leakage of server-only audit fields into browser payloads.

## 9) Feature-Flag / Capability Verification

Before and after enablement:
1. Confirm resolved capability profile hash equals registered hash.
2. Confirm policy groups are applied as expected:
   - animation policy
   - sound policy
   - localization policy
   - history policy
   - wallet display policy
   - feature policy
   - session UI policy
3. Confirm restricted features behave correctly by player context (for example buy-feature restrictions where applicable).

## 10) Artifact Version Pin Verification

Validate for sampled sessions:
1. Session bootstrap references the expected `clientPackageVersion`.
2. Runtime requests bind to expected `mathPackageVersion` via bootstrap reference.
3. RTP model selection follows registered/default rules.
4. Pinned values do not change within an active session.

## 11) Operational Evidence Pack (Per Rollout)

Store one evidence folder per rollout with:
1. registration verification output
2. canary validation logs
3. restore validation logs
4. history validation outputs
5. feature/capability verification results
6. artifact pin verification records
7. rollback rehearsal evidence (or explicit N/A with approval)

## 12) Ownership and Escalation

1. GS Ops owns rollout state transitions and operational sign-off.
2. Gamesv1 owns release-pack generation and artifact integrity inputs.
3. Incident commander owns go/no-go and rollback trigger decision during window.
