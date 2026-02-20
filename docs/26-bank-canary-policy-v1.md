# Bank Canary Policy v1

Last updated: 2026-02-20 UTC
Scope: GS modernization phased rollout with strict backward compatibility.

## 1) Canary Waves
1. Wave A (internal test bank): one low-risk bank in refactor stack for parity and telemetry checks.
2. Wave B (limited production canary): 1-2 low-volume banks.
3. Wave C (expanded): top 5 medium-volume banks after stable window.
4. Global rollout only after all gates pass.

## 2) Entry Gates (Must Pass)
1. Phase 0 parity tests pass for launch/wager/settle/history/FRB/reconnect.
2. Mandatory telemetry fields visible: `traceId`, `sessionId`, `bankId`, `gameId`, `operationId`, `configVersion`.
3. Rollback switch validated for target bank.
4. No unresolved critical incidents for last 24h in canary environment.
5. Session-service event contract check passes for canary topic payloads.

## 3) Exit Gates Per Wave
1. Launch success rate >= 99.5% (excluding upstream outage class).
2. Financial mismatch count = 0 critical, 0 unresolved high.
3. No increase in API error ratio beyond +0.5% absolute from baseline.
4. Reconnect success rate >= 99.0% for valid sessions.
5. No sustained p95 latency regression > 15% vs baseline.
6. Session-service outbox backlog (`NEW` + `RETRY`) does not grow continuously for 15 minutes.
7. Session-service DLQ count increase = 0 for canary window.
8. Replay-window throttling (`429` on forced rapid replay) remains below 3 events/hour.

## 4) Rollback Triggers (Immediate)
1. Any duplicate debit/credit event confirmed.
2. Any protocol contract break detected for Casino Side/MP/New Games.
3. Launch failure spike > 1.5% absolute sustained for 15 minutes.
4. Config propagation inconsistency across nodes affecting gameplay limits/coins.
5. Critical websocket/session ownership mismatch regression.
6. DLQ spike for session events in canary bank after route enable.

## 5) Rollback Procedure (Bank-Scoped)
1. Toggle bank traffic back to legacy handler path.
2. Freeze new config publishes for affected bank.
3. Preserve all telemetry + operation logs for incident replay.
4. Run focused parity subset to confirm recovery.
5. Open root-cause record before re-entry attempt.

## 6) Ownership Matrix
- Compatibility owner: parity and contract checks.
- Financial integrity owner: idempotency/reconciliation.
- Ops owner: rollout/rollback execution.
- Architecture owner: gate approval between waves.

## 7) Gate Commands (Refactor Canary)
1. Contract check:
   - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-event-contract-check.sh --topic abs.session.events.v1`
2. Canary route + shadow write:
   - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-canary-probe.sh --bank-id 6275`
   - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh --bank-id 6275`
3. Outbox state check:
   - `GET http://127.0.0.1:18073/api/v1/outbox?status=NEW`
   - `GET http://127.0.0.1:18073/api/v1/outbox?status=RETRY`
   - `GET http://127.0.0.1:18073/api/v1/outbox?status=DLQ`
4. Outbox alert thresholds:
   - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-outbox-alert-check.sh --max-new 10 --max-retry 2 --max-dlq 0 --sample-count 4 --sample-interval-sec 300`
   - uses strict trend check (fails if NEW or RETRY is strictly increasing across all samples).
5. DLQ replay operation (manual approval only):
   - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-dlq-replay.sh --event-id <eventId> --reason canary-replay`
   - replay guard uses `SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT` from cluster config.
6. Replay report snapshot:
   - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-dlq-report.sh --limit 20`
   - replay cooldown is controlled by `SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS`.
