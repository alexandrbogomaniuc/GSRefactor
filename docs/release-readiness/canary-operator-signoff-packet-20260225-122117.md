# Canary Operator Sign-Off Packet (Refactor GS Modernization)

- status: READY_FOR_HUMAN_SIGNOFF
- scope: Controlled deploy/canary approval for refactor environment
- prepared_by: Codex technical audit/finalization pass
- prepared_at_utc: 2026-02-25T12:21:32Z
- canary_wave: Wave A / limited canary
- selected_canary_bank_ids: 6275 (recommended default applied)
- rollout_style: limited monitored canary window (recommended default applied)

## 1. Plain-English Summary

This project is now technically ready for a controlled canary release.

What this means:
- The main modernization work is complete.
- The runtime tests that were previously blocking release are now passing.
- The security dependency audit work that was previously blocking release is now complete.
- The system is marked **GO_FOR_DEPLOY_AND_CANARY** in the latest readiness report.

This packet is the final approval package for human operators/managers to sign before the canary window starts.

## 2. Final Technical Readiness Decision (Already Verified)

**Decision:** `GO_FOR_DEPLOY_AND_CANARY`

Primary evidence (latest):
- Program readiness report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/program-deploy-readiness-status-20260225-121221.md`
- Phase 4 protocol status (GO):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol/phase4-protocol-status-report-20260225-121213.md`
- Phase 5/6 extraction status (GO):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-6/phase5-6-service-extraction-status-report-20260225-121213.md`
- Security hardening status (GO):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/security/security-hardening-status-report-20260225-121213.md`
- Security audit summary (production dependencies = clean):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/security/dependency-audit/audit-summary-prod.json`
- Legacy mixed-topology manual full-flow pass:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/validation/legacy-mixed-topology/legacy-mixed-topology-manual-full-flow-20260224-162730.md`

## 3. Selected Canary Plan (Recommended Defaults Applied)

### Chosen plan (applied for this sign-off packet)
- Canary bank: `6275` only
- Canary type: limited monitored canary window
- Rollback model: bank-scoped rollback first (do not widen scope during incident)

### Why this is the recommended choice
- It is the lowest-risk way to start.
- It proves the real production path without exposing more banks than needed.
- It keeps rollback simple and fast.

### Next expansion (only after successful canary)
- Add bank `6274` as the next canary bank.

## 4. Human Sign-Off Questions (with Recommended and Alternative Answers)

### Question A: Which bank should be used for the first canary?
- Recommended answer: `6275 only`
- Alternative answer: `6274 and 6275 together`
- Alternative answer: `custom bank list` (write exact bank IDs below)

Selected now: `6275`

### Question B: What rollout style should be used?
- Recommended answer: `limited monitored canary window`
- Alternative answer: `internal-only rehearsal deploy first`
- Alternative answer: `broader rollout immediately`

Selected now: `limited monitored canary window`

### Question C: What sign-off format should be used?
- Recommended answer: `this packet + latest evidence links`
- Alternative answer: `this packet + separate internal ops template`
- Alternative answer: `readiness report only` (not recommended)

Selected now: `this packet + latest evidence links`

## 5. Launch URL (Operator Smoke Check)

Use the alias URL (no legacy endpoint name shown in browser URL):

- VND canary bank `6275`:
  - [http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en](http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en)

Notes:
- `subCasinoId=507` is required on localhost testing for these banks.
- Browser URL uses `/startgame` alias.

## 6. Pre-Canary Checklist (Simple English)

Mark each item before the canary starts.

- [ ] Change window is approved (date/time recorded below)
- [ ] Rollback owner is assigned and reachable
- [ ] Monitoring owner is assigned and reachable
- [ ] Support/ops contact is informed of canary start and stop window
- [ ] Final readiness report reviewed (`GO_FOR_DEPLOY_AND_CANARY`)
- [ ] Phase 4 protocol status reviewed (`GO`)
- [ ] Phase 5/6 service extraction status reviewed (`GO`)
- [ ] Security status reviewed (`GO`, audit clean)
- [ ] Legacy mixed-topology manual full-flow pass reviewed
- [ ] Bank `6275` selected and confirmed
- [ ] Rollback trigger thresholds reviewed (section 9 below)

## 7. Canary Start Procedure (Operator Steps)

1. Confirm the change window is active.
2. Confirm the refactor services are running and healthy.
3. Open the launch URL in section 5 and confirm the game launches.
4. Start canary traffic for bank `6275` only.
5. Watch the monitoring checks in section 8 for the full canary window.
6. If any rollback trigger happens, stop the canary and use the rollback steps in section 10.
7. If the window completes cleanly, record the result in section 11 and sign off.

## 8. Live Monitoring During Canary (What to Watch)

### What good looks like (simple English)
- Players can launch games normally.
- No wallet errors (reserve/settle problems).
- No growing error backlog.
- No sudden error spike.
- No financial mismatch alerts.

### Useful checks (from project runbooks/policies)
- Readiness/report baseline:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/program-deploy-readiness-status-20260225-121221.md`
- Canary policy (full gate/rollback policy):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/26-bank-canary-policy-v1.md`
- Session outbox checks (if used in your canary watch):
  - `GET http://127.0.0.1:18073/api/v1/outbox?status=NEW`
  - `GET http://127.0.0.1:18073/api/v1/outbox?status=RETRY`
  - `GET http://127.0.0.1:18073/api/v1/outbox?status=DLQ`

## 9. Immediate Rollback Triggers (Simple English)

Stop the canary immediately if any of these happen:
- Duplicate debit/credit (money counted twice)
- Launch failures spike and stay elevated
- Wallet/reserve/settle errors become visible for canary bank
- Protocol compatibility break with Casino Side / MP / New Games
- Session or websocket ownership mismatch causing broken gameplay
- Outbox/DLQ errors start growing for canary bank

## 10. Rollback Procedure (Bank-Scoped)

1. Move the canary bank (`6275`) back to the previous stable path.
2. Stop widening rollout (do not add more banks).
3. Freeze config changes for the affected bank until review is done.
4. Save logs/evidence for incident review.
5. Confirm recovery with a basic launch/wallet check.
6. Record the incident and result before any retry.

## 11. Canary Result Record (Fill During/After Window)

- Change window start (UTC): ____________________
- Change window end (UTC): ____________________
- Canary bank(s): `6275` / other: ____________________
- Rollback used? (Yes/No): ____________________
- If yes, reason: ____________________
- Launch result summary: ____________________
- Wallet/financial result summary: ____________________
- Error/incident summary: ____________________
- Final canary outcome (PASS / FAIL / PARTIAL): ____________________
- Decision after canary:
  - [ ] Keep canary running
  - [ ] Roll back
  - [ ] Expand to next bank (`6274`)
  - [ ] Require more investigation

## 12. Human Sign-Off Table

### Technical Readiness (prepared)
- Status: Completed by evidence package (`GO_FOR_DEPLOY_AND_CANARY`)
- Prepared by: Codex
- Ready at (UTC): 2026-02-25T12:21:32Z

### Required human approvals (fill in)
- Ops / Rollout Owner name: ____________________
- Ops / Rollout Owner sign-off (Yes/No): ____________________
- Monitoring Owner name: ____________________
- Monitoring Owner sign-off (Yes/No): ____________________
- Financial Integrity Owner name: ____________________
- Financial Integrity Owner sign-off (Yes/No): ____________________
- Compatibility Owner name: ____________________
- Compatibility Owner sign-off (Yes/No): ____________________
- Final release approver name: ____________________
- Final release approver sign-off (Yes/No): ____________________
- Final sign-off timestamp (UTC): ____________________

## 13. Final Note (Plain English)

At this point, the remaining step is not coding. It is the human approval and controlled rollout decision.

This packet is ready to be used as the sign-off record for the first canary window.
