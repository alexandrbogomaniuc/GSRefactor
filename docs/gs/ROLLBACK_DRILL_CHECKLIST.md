# GS Rollback Drill Checklist

- Scope: Operational rollback rehearsal for productized slot releases
- Non-goals: No architecture/contract changes
- Related:
  - `docs/gs/enable-disable-canary-rollback.md`
  - `docs/gs/PRODUCTIZED_GAME_ROLLOUT_RUNBOOK.md`

## 1) Drill Metadata

- Game ID:
- Candidate Release ID:
- Rollback Target Release ID:
- Environment:
- Drill Start (UTC):
- Drill Lead:

## 2) Preconditions

- [ ] Candidate release registered and currently canary/enabled in drill scope.
- [ ] Rollback target release registered and validated.
- [ ] Monitoring dashboard and alerting active.
- [ ] Incident communication channel open.
- [ ] Test accounts and scenario scripts prepared.

## 3) Baseline Snapshot (Before Rollback)

- [ ] Capture current rollout state and routing snapshot.
- [ ] Capture active session count and health metrics.
- [ ] Capture error-rate baseline.
- [ ] Capture wallet/settlement baseline metrics.

## 4) Rollback Execution Steps

- [ ] Mark candidate release non-active (`DISABLED` or equivalent in state model).
- [ ] Mark rollback target as active (`ROLLBACK_TARGET -> ENABLED`).
- [ ] Refresh/reload GS config caches.
- [ ] Confirm routing points new launches to rollback target.

## 5) Functional Verification After Rollback

- [ ] Launch/bootstrap works on rollback target.
- [ ] `openGame` works on rollback target.
- [ ] `playRound` works and settles correctly.
- [ ] `featureAction` works for relevant feature paths.
- [ ] `getHistory` returns expected data.
- [ ] `closeGame` works.

## 6) Session Restore Verification During Drill

- [ ] Start round, disconnect, and invoke restore path.
- [ ] Resume succeeds using persisted snapshot.
- [ ] Restored continuation deterministic.
- [ ] `requestCounter`/`stateVersion` continuity maintained.

## 7) Capability / Pin Verification After Rollback

- [ ] New sessions show rollback target client package pin.
- [ ] New sessions show rollback target math package pin.
- [ ] RTP model resolution follows rollback target registration rules.
- [ ] Capability hash matches rollback target profile.
- [ ] Existing sessions are not force-mutated mid-session.

## 8) Pass/Fail Criteria

Pass requires all:
- [ ] Rollback completed inside operational time budget.
- [ ] No critical financial integrity issue observed.
- [ ] No restore determinism regression observed.
- [ ] No critical launch/runtime failure observed.
- [ ] Observability and incident trail complete.

If any fail item occurs:
- [ ] Keep traffic on stable target release.
- [ ] Open incident with severity assignment.
- [ ] Capture full evidence package and owner actions.

## 9) Drill Evidence Package

- [ ] Rollout transition log
- [ ] API validation logs
- [ ] Restore scenario logs
- [ ] History scenario logs
- [ ] Capability/pin verification output
- [ ] KPI before/after snapshots
- [ ] Incident timeline (if applicable)

## 10) Sign-off

- Drill Result: PASS / FAIL
- Completed By:
- Reviewed By (Ops Lead):
- Reviewed By (Engineering Lead):
- Follow-up Actions:
- Follow-up Due Date:
