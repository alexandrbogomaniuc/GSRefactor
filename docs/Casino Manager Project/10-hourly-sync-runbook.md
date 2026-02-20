# Hourly Sync Runbook

## Schedule
- Cron frequency: once per hour.
- Cron expression:
  - `0 * * * *`

## Cron Entry (reference)
```cron
0 * * * * /opt/cm/bin/cm-sync-hourly.sh >> /var/log/cm-sync-hourly.log 2>&1
```

## Job Steps
1. Acquire distributed lock:
   - lock key: `cm_sync_hourly`.
2. Read checkpoint from `cm_sync.checkpoints`.
3. For each phase-1 read model:
   - extract changed source rows since checkpoint,
   - transform/decode to CM read shape,
   - idempotent upsert into `cm_read.*`.
4. Persist run audit row in `cm_sync.audit_runs`.
5. Advance checkpoint only if all tables succeeded.
6. Release lock.

## Table Order
1. `cm_read.bank_list_by_subcasino`
2. `cm_read.player_search_by_bank`
3. `cm_read.game_session_by_id`
4. `cm_read.transactions_by_bank_day`
5. `cm_read.wallet_alerts_by_bank_day`

## Operational SLO (phase-1)
- Sync completion target:
  - `< 10 min` per hourly run.
- Allowed staleness:
  - up to `60 min`.
- Error budget:
  - no more than `2` failed hourly runs in `24h`.

## Retry and Failure Policy
- Retry each failed extract/apply step up to `3` times.
- Backoff: `30s`, `90s`, `180s`.
- If still failing:
  - mark run `FAILED`,
  - keep previous checkpoint,
  - send alert,
  - continue next scheduled run.

## Reconciliation
- Daily reconcile job:
  - compare row counts/checksum buckets between source and `cm_read`.
  - emit drift report under project evidence.

## Safety Rules
- CM app DB user must be read-only on `cm_read`.
- Sync worker has write access only to `cm_read` and `cm_sync`.
- No UI-triggered manual full refresh in phase-1.
