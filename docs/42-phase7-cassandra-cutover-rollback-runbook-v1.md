# Phase 7 Cassandra Cutover + Rollback Runbook v1

Last updated: 2026-02-20 UTC

## 1) Cutover prerequisites
- Rehearsal checklist completed and signed.
- Backups/snapshots verified and restorable.
- Canary bank list approved.
- Incident channel and rollback owner assigned.

## 2) Cutover steps (bank-scoped first)
1. Enter change window and freeze config publishes.
2. Confirm source/target cluster health.
3. Point canary-bank traffic to target Cassandra path.
4. Run parity smoke suite:
- launch, wager, settle, history, FRB, reconnect.
5. Observe for stability window (minimum 30 minutes) before expanding.

## 3) Live monitors during canary
- API error ratio delta.
- Financial mismatch/duplicate operations.
- Session reconnect failures.
- Cassandra read/write latency and timeout rate.
- Outbox/DLQ trend for extracted services.

## 4) Immediate rollback triggers
- Any confirmed duplicate debit/credit.
- Protocol contract break.
- Sustained launch failure spike.
- Data inconsistency affecting gameplay/config values.

## 5) Rollback steps
1. Repoint canary-bank traffic back to previous Cassandra cluster.
2. Freeze new writes on failed target path.
3. Preserve logs, traces, and operation records.
4. Re-run focused parity checks to confirm recovery.
5. Open RCA and remediation before re-attempt.

## 6) Post-window closure
- Publish outcome report with metrics and incidents.
- Update compatibility matrix and runbook deltas.
