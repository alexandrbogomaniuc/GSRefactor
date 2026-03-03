# GS Enable / Disable / Canary / Rollback Runbook for New Slots

- Status: Draft for operations (Phase 1)
- Date: 2026-03-01
- Scope: GS Ops rollout control for slot releases registered via `slot-release-registration-v1`
- Related:
  - `docs/gs/release-registration-contract.md`
  - `docs/gs/browser-runtime-api-contract.md`
  - `docs/gs-release-runtime-integration-flow.md`

## 1) Purpose

Define precise operational flow for staging registration, smoke testing, canary enablement, full production enablement, and rollback for new slot releases.

## 2) State Machine

Allowed rollout states:

- `DISABLED`
- `CANARY`
- `ENABLED`
- `ROLLBACK_TARGET`

Allowed transitions:

1. `DISABLED -> CANARY`
2. `DISABLED -> ENABLED` (only when explicitly approved to skip canary)
3. `CANARY -> ENABLED`
4. `CANARY -> DISABLED`
5. `ENABLED -> ROLLBACK_TARGET` (select target)
6. `ROLLBACK_TARGET -> ENABLED` (activate rollback target)

No other transition is allowed.

## 3) Operational Flow

## 3.1 Staging Registration

Inputs:
- Gamesv1 release-pack outputs:
  - `release-registration.json`
  - `client-artifact-manifest.json`
  - `math-package-manifest.json`
  - `release-evidence.md`

Actions:
1. Register release object in GS staging config store.
2. Verify signature/hash integrity for client and math artifacts.
3. Validate compatibility gates (`minGsVersion`, contract versions).
4. Keep state `DISABLED`.

Exit criteria:
- Registration stored and validation passes with zero blockers.

## 3.2 Smoke Test

Actions:
1. Set route scope to staging-only bank/subcasino and set release state to `CANARY` with tiny scope (`trafficPercent=1` or strict allowlist).
2. Execute launch + runtime smoke:
   - launch handoff
   - `bootstrap`
   - `opengame`
   - `playround`
   - `featureaction` (if applicable)
   - `gethistory`
   - `closegame`
3. Verify wallet reserve/settle and persistence rows.
4. Verify reconnect with unfinished-round restore.

Required pass gates:
- no `INVALID_REQUEST_COUNTER` drift under normal sequence,
- no idempotency-key reuse faults under duplicate retry test,
- no deterministic restore mismatch,
- no legacy-route regression.

## 3.3 Canary Enablement

Actions:
1. Move release state to `CANARY`.
2. Configure explicit canary scope:
   - banks/subcasinos allowlist and/or bounded percentage.
3. Keep prior production release as active fallback target.
4. Monitor canary KPIs for defined observation window.

Canary KPIs (minimum):
- launch success rate,
- wallet reserve/settle success rate,
- duplicate/idempotency conflict rate,
- unfinished-round restore success rate,
- error code distribution drift (`5xx`, `409`, `401`).

Promote criteria:
- KPI thresholds within approved limits and no critical incidents.

## 3.4 Production Enablement

Actions:
1. Promote canary release to `ENABLED`.
2. Keep previous release registered as rollback target candidate.
3. Freeze immutable session values for newly opened sessions:
   - `clientPackageVersion`
   - `mathPackageVersion`
   - selected `rtpModelId`
   - `capabilityProfileHash`

Rules:
- Existing active sessions remain on their originally pinned versions.
- Do not force-switch active sessions to new release artifacts.

## 3.5 Rollback

Precondition:
- Rollback target release is registered and valid.

Actions:
1. Mark current faulty release `DISABLED` or keep as non-active.
2. Set target release state `ROLLBACK_TARGET`.
3. Activate target release by transition `ROLLBACK_TARGET -> ENABLED`.
4. Refresh GS route/config caches.
5. Re-run rollback smoke:
   - launch/bootstrap/opengame/playround/gethistory/closegame.
6. Confirm new sessions open on rollback target while active old sessions continue with pinned values.

Mandatory rollback safeguards:
- no mutation of immutable values for active sessions,
- no launch-time math override,
- no deletion of audit trail or release registration records.

## 4) Disable Flow (Kill Switch)

Emergency disable actions:
1. Set release state to `DISABLED`.
2. Route new launches to previous enabled release (or legacy route if needed).
3. Preserve read/close behavior for already-active sessions.

Use when:
- severe wallet/persistence integrity issue,
- deterministic restore failure,
- critical production incident requiring immediate traffic stop.

## 5) Immutable Session Enforcement

GS must enforce for active session:

1. `clientPackageVersion` immutable
2. `mathPackageVersion` immutable
3. selected `rtpModelId` immutable
4. `capabilityProfileHash` immutable

Any mismatch attempt should return `BOOTSTRAP_CONFIG_MISMATCH` or `MATH_PACKAGE_MISMATCH` and force re-bootstrap/new session flow.

## 6) Required Persistence for Unfinished-Round Restore

For each active session, GS persistence must include:

1. `sessionId`
2. `releaseId`
3. `requestCounter`
4. `stateVersion`
5. `restoreSnapshotBlob`
6. `clientPackageVersion`
7. `mathPackageVersion`
8. selected `rtpModelId`
9. `capabilityProfileHash`

Deterministic restore is invalid without this set.

## 7) Operational Checklist

Before canary:
1. Release registration validated and signed.
2. Artifact hashes verified.
3. Smoke test pass evidence attached.

Before production enable:
1. Canary KPIs pass.
2. Rollback target pre-registered.
3. On-call and incident runbook confirmed.

After production enable:
1. Session pinning checks pass.
2. Error-rate baseline unchanged within threshold.
3. Rollback drill verified.

## 8) Minimum Rollback Drill

At least once per release candidate:
1. Move candidate to `CANARY`.
2. Promote to `ENABLED` in controlled test scope.
3. Roll back to prior target in one operation window.
4. Verify new launches use rollback target and active sessions remain stable.

